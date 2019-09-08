package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.commons.bean.Resp;
import com.atguigu.gulimall.order.vo.order.OrderCloseVo;
import com.atguigu.gulimall.order.vo.order.OrderEntityVo;
import com.atguigu.gulimall.order.vo.order.OrderFeignSubmitVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单的所有业务。crud，正常业务
 */
@FeignClient("gulimall-oms")
public interface OrderCreateFeignService {

    /**
     * 创建订单
     * @param vo
     * @return
     */
    @PostMapping("/oms/order/createAndSave")
    public Resp<OrderEntityVo> createAndSaveOrder(@RequestBody OrderFeignSubmitVo vo);

    /**
     * 查询订单
     * @param id
     * @return
     */
    @GetMapping("/oms/order/info/{id}")
    public Resp<OrderEntityVo> info(@PathVariable("id") Long id);


    /**
     * 关闭订单
     * @param order
     * @return
     */
    @PostMapping("/oms/order/update")
    public Resp<Object> closeOrder(@RequestBody OrderCloseVo order);


    @PostMapping("/oms/order/update")
    public Resp<Object> updateOrder(@RequestBody OrderEntityVo order);


    @PostMapping("/oms/order/payed")
    public Resp<Object> payedOrder(@RequestBody OrderEntityVo order);

}
