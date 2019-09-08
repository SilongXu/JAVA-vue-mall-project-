package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.constant.BizCode;
import com.atguigu.gulimall.commons.constant.RabbitMQConstant;
import com.atguigu.gulimall.commons.to.mq.OrderMqTo;
import com.atguigu.gulimall.order.enume.OrderStatusEnume;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberAddressFeignService;
import com.atguigu.gulimall.order.feign.OrderCreateFeignService;
import com.atguigu.gulimall.order.feign.WareHourseFeignService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.*;
import com.atguigu.gulimall.order.vo.cart.ClearCartSkuVo;
import com.atguigu.gulimall.order.vo.order.OrderEntityVo;
import com.atguigu.gulimall.order.vo.order.OrderFeignSubmitVo;
import com.atguigu.gulimall.order.vo.pay.PayAsyncVo;
import com.atguigu.gulimall.order.vo.ware.LockStockVo;
import com.atguigu.gulimall.order.vo.ware.SkuLock;
import com.atguigu.gulimall.order.vo.ware.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);



    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    MemberAddressFeignService memberAddressFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareHourseFeignService wareHourseFeignService;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderCreateFeignService orderCreateFeignService;

    @Autowired
    JedisPool jedisPool;

    @Override
    public Order createOrder() {
        Order order = new Order();
        order.setOrderId(IdWorker.getId());
        order.setDesc("商品xxxxds");
        order.setStatus(0);

        //订单创建完成就给MQ发送一条消息
//        rabbitTemplate.convertAndSend("orderCreateExchange","create.order",order);


        //利用定时线程池
        //有啥问题？
        executorService.schedule(() -> {
            System.out.println(order + "已经过期，正准备查询数据库，决定是否关单");
        }, 30, TimeUnit.SECONDS);


//        executorService.scheduleAtFixedRate()
        return order;


    }

    @Override
    public OrderConfirmVo confirmOrderData(Long id) {

        System.out.println("服务进来的主线程号...." + Thread.currentThread().getId());
        OrderConfirmVo vo = new OrderConfirmVo();

        //1、获取到原来的请求
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        //1、封装远程的用户地址信息
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            //封装远程的用户地址信息
            //利用ThreadLocal在同一线程共享数据....
            RequestContextHolder.setRequestAttributes(requestAttributes);
            System.out.println("memberAddressFeignService服务进来的线程号...." + Thread.currentThread().getId());
            Resp<List<MemberAddressVo>> resp = memberAddressFeignService.memberAddress(id);
            vo.setAddresses(resp.getData());
        }, executor);


        //2、封装购物车信息，远程调用这是发一个新请求，new request。就会丢失之前的请求头信息；
        //1）、改造传用户id
        //2）、feign；写一个拦截器 RequestInterceptor; 在发送请求之前对请求做定制化修改


        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("cartFeignService 服务进来的线程号...." + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            Resp<CartVo> cartVoResp = cartFeignService.getCartCheckItemsAndStatics();
            vo.setCartVo(cartVoResp.getData());
        }, executor);

        // vo.setCartVo();
        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future);
            allOf.get();

            //创建一个交易令牌，以后提交订单都要携带；
            String orderToken = IdWorker.getTimeId();

            redisTemplate.opsForValue().set(Constant.TOKENS + orderToken, orderToken, Constant.TOKENS_TIMEOUT, TimeUnit.MINUTES);
            vo.setOrderToken(orderToken);
            return vo;
        } catch (Exception e) {
            System.out.println();
            e.printStackTrace();
        }


        return vo;

    }

    @Override
    public Resp<Object> submitOrder(OrderSubmitVo vo, Long userId) {

        //0、验是否重复提交；【原子性。】
        String orderToken = vo.getOrderToken();
        /**
         *   redis-cli
         *      >  get aaaaa
         *      >  if(aaaaa == args){
         *      >  del aaaa
         *        }
         *      >
         */
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //RedisScript<T> script, List<K> keys, Object... args
//        Integer execute = redisTemplate.execute(new DefaultRedisScript<>(script,Integer.class), Arrays.asList(Constant.TOKENS + orderToken), orderToken);
        Jedis jedis = jedisPool.getResource();
        Long execute = (Long) jedis.eval(script, Arrays.asList(Constant.TOKENS + orderToken), Arrays.asList(orderToken));

        try {
            if (execute == 1) {
                //redis里面有令牌，并且验证通过，删除了，
                //1、验库存\锁库存。(具有原子性，防止刚验库存有东西，等锁又没了)
                //1.1）、获取到我们想要买的所有东西（从购物车中直接获取）
                Resp<CartVo> itemsAndStatics = cartFeignService.getCartCheckItemsAndStatics();

                CartVo cartVo = itemsAndStatics.getData();
                List<CartItemVo> items = cartVo.getItems();
                List<SkuLockVo> skuIds = new ArrayList<>();//准备将所有需要验库存的商品id发过去

                items.forEach((itemVo) -> {
                    SkuLockVo skuLockVo = new SkuLockVo();
                    skuLockVo.setSkuId(itemVo.getSkuId());
                    skuLockVo.setNum(itemVo.getNum());
                    skuLockVo.setOrderToken(orderToken);
                    skuIds.add(skuLockVo);
                });

                //1.2）、验库存同时锁库存；
                Resp<LockStockVo> resp = null;
                try{
                    resp = wareHourseFeignService.lockAndCheckStock(skuIds);
                }catch (Exception e){
                    Resp<Object> fail = Resp.fail(null);
                    fail.setCode(BizCode.SERVICE_UNAVAILABLE.getCode());
                    fail.setMsg(BizCode.SERVICE_UNAVAILABLE.getMsg());
                    return fail;
                }


                System.out.println("返回的数据..." + resp.getData());
                if (resp.getData().getLocked() == true) {
                    //库存全部锁住
                    //2、验价。前端提交的价和购物车选中的价进行对比，如果一样才可以；
                    BigDecimal totalPrice = vo.getTotalPrice();
                    //最新查询到的购物车的价格信息；
                    Resp<CartVo> cartVoResp = cartFeignService.getCartCheckItemsAndStatics();

                    int i = cartVoResp.getData().getCartPrice().compareTo(totalPrice);
                    if (i != 0) {
                        //验价失败....
                        Resp<Object> fail = Resp.fail(null);
                        fail.setCode(BizCode.ORDER_NEED_REFRESH.getCode());
                        fail.setMsg(BizCode.ORDER_NEED_REFRESH.getMsg());
                        return fail;
                    }
                    //3、生成订单,保存订单所有的订单项；
                    //远程生成保存订单与订单项信息；
                   //cartVoResp
                    OrderFeignSubmitVo orderFeignSubmitVo = new OrderFeignSubmitVo();


                    BeanUtils.copyProperties(vo,orderFeignSubmitVo);
                    orderFeignSubmitVo.setCartVo(cartVoResp.getData());
                    Long addressId = vo.getAddressId();
                    Resp<MemberAddressVo> info = memberAddressFeignService.info(addressId);
                    MemberAddressVo data = info.getData();

                    orderFeignSubmitVo.setReceiverName(data.getName());
                    orderFeignSubmitVo.setReceiverDetailAddress(data.getDetailAddress());
                    orderFeignSubmitVo.setReceiverPhone(data.getPhone());
                    orderFeignSubmitVo.setOrderToken(orderToken);
                    orderFeignSubmitVo.setUserId(userId);

                    Resp<OrderEntityVo> saveOrder = null;
                    try{
                        //创建订单，如果远程服务正好调用成功了，但是返回数据途中服务器崩了。。。
                        //在远程服务订单创建好久给mq发送创建成功的消息
                        saveOrder = orderCreateFeignService.createAndSaveOrder(orderFeignSubmitVo);
                        //订单创建成功了....
                        //订单的倒计时过期时间...
                    }catch (Exception e){

                        //远程调用失败....
                        Resp<Object> fail = Resp.fail(null);
                        fail.setCode(BizCode.SERVICE_UNAVAILABLE.getCode());
                        fail.setMsg(BizCode.SERVICE_UNAVAILABLE.getMsg());

                        //解锁库存；
                        LockStockVo data1 = resp.getData();
                        List<SkuLock> locks = data1.getLocks();
                        //远程服务进行解锁....
                        //远程调用。如果代码走到这里正好炸了，远程服务就感知不到，哪些需要解锁；

                        //最终一致性；
                        return fail;
                    }


                    //5、订单创建完清除购物车选中的商品；
                    List<Long> clearSkuIds = new ArrayList<>();
                    cartVoResp.getData().getItems().forEach((itemVo)->{
                        clearSkuIds.add(itemVo.getSkuId());
                    });
                    //远程清除购物车
                    ClearCartSkuVo skuVo = new ClearCartSkuVo();
                    skuVo.setSkuIds(clearSkuIds);
                    skuVo.setUserId(userId);
//                   cartFeignService.clearSkuIds(skuVo);

                    //6、支付成功才扣库存



                    return Resp.ok(saveOrder.getData());

                } else {
                    Resp<Object> fail = Resp.fail(null);
                    fail.setCode(BizCode.STOCK_NOT_ENOUGH.getCode());
                    fail.setMsg(BizCode.STOCK_NOT_ENOUGH.getMsg());
                    //获取到所有库存不够的数据
                    List<SkuLock> locks = resp.getData().getLocks();
                    List<Long> stockNoSkuIds = new ArrayList<>();
                    locks.forEach((item) -> {
                        Boolean success = item.getSuccess();
                        if (!success) {
                            stockNoSkuIds.add(item.getSkuId());
                        }
                    });
                    Map<String, List<Long>> map = new HashMap<>();
                    map.put("notEnoughStockSkus", stockNoSkuIds);
                    fail.setData(map);
                    return fail;
                }


            } else {
                //令牌验证失败
                Resp<Object> fail = Resp.fail(null);
                fail.setCode(BizCode.TOKEN_INVAILIED.getCode());
                fail.setMsg(BizCode.TOKEN_INVAILIED.getMsg());
                return fail;
            }
        } finally {
            jedis.close();
        }


    }

    @Override
    public void paySuccess(PayAsyncVo vo) {


        //订单号
        String tradeNo = vo.getOut_trade_no();

        //告诉消息队列此订单支付成了...
        OrderMqTo mqTo = new OrderMqTo();
        mqTo.setOrderSn(tradeNo);
        //修改订单状态
        log.info("支付成了...."+vo);
        OrderEntityVo orderEntityVo = new OrderEntityVo();
        orderEntityVo.setOrderSn(tradeNo);
        orderEntityVo.setStatus(OrderStatusEnume.PAYED.getCode());
        orderCreateFeignService.payedOrder(orderEntityVo);

        rabbitTemplate.convertAndSend(RabbitMQConstant.order_exchange,RabbitMQConstant.order_pay_success_routing_key,mqTo);


    }


    //@RabbitListener(queues = "closeOrderQueue")
//    public void closeOrder(Order order, Channel channel, Message message) throws IOException {
//
//        System.out.println("收到的订单内容：" + order);
//
//        Long orderId = order.getOrderId();
//        System.out.println("正在数据库查询【" + orderId + "】订单状态，" + order.getStatus());
//
//        if (order.getStatus() != 1) {
//            System.out.println("这个订单没有被支付，正在准备关闭。。。数据库状态改为-1");
//        }
//
//
//        //给MQ回复，我们已经处理完成此消息
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//
//    }
}
