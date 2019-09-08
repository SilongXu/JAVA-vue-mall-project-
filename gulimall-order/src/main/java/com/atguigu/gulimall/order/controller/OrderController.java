package com.atguigu.gulimall.order.controller;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.commons.utils.GuliJwtUtils;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.Order;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.order.OrderEntityVo;
import com.atguigu.gulimall.order.vo.pay.PayVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {


    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;


    @ApiOperation("订单确认信息")
    @GetMapping("/confirm")
    public Resp<OrderConfirmVo> orderConfirm(HttpServletRequest request){

        String authorization = request.getHeader("Authorization");
        Map<String, Object> body = GuliJwtUtils.getJwtBody(authorization);
        long id = Long.parseLong(body.get("id").toString());


        OrderConfirmVo confirmVo =  orderService.confirmOrderData(id);

        return Resp.ok(confirmVo);
    }



    @PostMapping("/submit")
    public String submitOrder(@RequestBody OrderSubmitVo vo,
                                    HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        Long userId = getCurrentUserId(request);

        Resp<Object> resp = orderService.submitOrder(vo, userId);
        Object data = resp.getData();
        if(data instanceof OrderEntityVo){
            //订单成功了...
            //生成一个支付页，等待支付
            OrderEntityVo order = (OrderEntityVo) data;
            PayVo payVo = new PayVo();
//            payVo.setBody(new String("谷粒商城".getBytes("UTF-8"),"UTF-8"));
//            payVo.setOut_trade_no(new String(order.getOrderSn().getBytes("UTF-8"),"UTF-8"));
//            payVo.setSubject(new String("谷粒商城收银台".getBytes("UTF-8"),"UTF-8"));
//            payVo.setTotal_amount(new String(order.getPayAmount().toString().getBytes("UTF-8"),"UTF-8"));
//            BigDecimal bigDecimal = new BigDecimal(order.getPayAmount().toString(), new MathContext(2));

            BigDecimal bigDecimal = order.getPayAmount().setScale(2);

            payVo.setTotal_amount(bigDecimal.toString());
            payVo.setOut_trade_no(order.getOrderSn());
            payVo.setSubject("谷粒商城收银台");
            payVo.setBody("payVo");
            String pay = alipayTemplate.pay(payVo);
            String s = new String(pay.getBytes("UTF-8"), "UTF-8");
            System.out.println("支付宝的编码的页面："+s);
            return pay;
        }
        return JSON.toJSONString(resp);
    }


    private Long getCurrentUserId(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        Map<String, Object> body = GuliJwtUtils.getJwtBody(authorization);
        long id = Long.parseLong(body.get("id").toString());
        return id;
    }

//    /**
//     * 创建订单
//     * @return
//     */
//    @GetMapping("/create")
//    public Order createOrder(){
//
//       Order order =  orderService.createOrder();
//
//       return order;
//    }
}
