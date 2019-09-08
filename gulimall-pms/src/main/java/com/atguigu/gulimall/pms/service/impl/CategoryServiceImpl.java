package com.atguigu.gulimall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.commons.bean.Constant;
import com.atguigu.gulimall.pms.annotation.GuliCache;
import com.atguigu.gulimall.pms.vo.CategoryWithChildrensVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.commons.bean.PageVo;
import com.atguigu.gulimall.commons.bean.Query;
import com.atguigu.gulimall.commons.bean.QueryCondition;

import com.atguigu.gulimall.pms.dao.CategoryDao;
import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.pms.service.CategoryService;
import org.springframework.util.StringUtils;


@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<CategoryEntity> getCategoryByLevel(Integer level) {

        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if(level != 0){
            wrapper.eq("cat_level",level);
        }



        List<CategoryEntity> entities = categoryDao.selectList(wrapper);

        return entities;
    }

    @Override
    public List<CategoryEntity> getCategoryChildrensById(Integer catId) {

        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_cid",catId);

        List<CategoryEntity> list = categoryDao.selectList(wrapper);

        return list;
    }

    /**
     * 可以编写一个Filter；
     * 利用AOP原理
     *
     * key； 前缀+id
     * product:1
     * catelog:1
     * @param id
     * @return
     */
    //@Cache(key="")
    @Override
    @GuliCache(prefix = Constant.CACHE_CATELOG)
    public List<CategoryWithChildrensVo> getCategoryChildrensAndSubsById(Integer id) {
       log.info("目标方法运行");
        System.out.println("service---线程..."+Thread.currentThread().getId());
        List<CategoryWithChildrensVo> vos = categoryDao.selectCategoryChildrenWithChildrens(id);
        /**
         * 1、缓存穿透：null值缓存，设置短暂的过期时间
         * 2、缓存雪崩：过期时间+随机值
         * 3、缓存击穿：分布式锁
         */
//        String s = redisTemplate.opsForValue().get(Constant.CACHE_CATELOG);
//        if(!StringUtils.isEmpty(s)){
//            log.info("菜单数据缓存命中...");
//            vos = JSON.parseArray(s, CategoryWithChildrensVo.class);
//        }else {
//            //1、缓存中没有，查数据库
//            log.info("菜单数据缓存没命中...正在查询数据库");
//            vos = categoryDao.selectCategoryChildrenWithChildrens(id);
//            //2、放到缓存中
//            redisTemplate.opsForValue().set(Constant.CACHE_CATELOG,JSON.toJSONString(vos));
//        }

        return vos;
    }

}