package com.atguigu.gulimall.search;

import com.atguigu.gulimall.search.vo.SearchParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

        Index build = new Index.Builder(new User("aaaaa","bbbbb",30)).index("user").type("info").id("1").build();


        DocumentResult execute = jestClient.execute(build);

        System.out.println("保存完成....");

    }


    @Test
    public void testSearchSourceBuilder(){
        SearchParam params = new SearchParam();

        params.setKeyword("手机");

        params.setBrand((Long[])Arrays.asList(4L,5L).toArray());

        params.setProps((String[])Arrays.asList("34:4-5","33:4000").toArray());

/**
 *     // order=0:asc  排序规则   0:asc
 *     private String order;// 0：综合排序  1：销量  2：价格
 */
        params.setOrder("2:desc");

        params.setPriceFrom(5000);
        params.setPriceTo(7000);
        String s = buildDSL(params);
        System.out.println(s);
    }


    private String buildDSL(SearchParam params) {
        //0、先获取一个SearchSourceBuilder，辅助我们得到DSL语句
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //1、查询&过滤
        BoolQueryBuilder bool = new BoolQueryBuilder();
        //1）、构造match条件
        if(!StringUtils.isEmpty(params.getKeyword())){
            MatchQueryBuilder match = new MatchQueryBuilder("name",params.getKeyword());
            bool.must(match);
        }
        //2）、构造过滤条件
        if(params.getBrand()!=null && params.getBrand().length>0){
            //2.1）、按照品牌过滤
            TermsQueryBuilder brand = new TermsQueryBuilder("brandId",params.getBrand());
            bool.filter(brand);
        }
        if(params.getCatelog3()!=null && params.getCatelog3().length>0){
            //2.2）、按照分类id过滤
            TermsQueryBuilder category = new TermsQueryBuilder("productCategoryId", params.getCatelog3());
            bool.filter(category);
        }
        if(params.getPriceFrom()!=null || params.getPriceTo()!=null){
            //2.3）、按照价格区间过滤
            RangeQueryBuilder price = new RangeQueryBuilder("price");
            if(params.getPriceFrom()!=null){
                price.gte(params.getPriceFrom());
            }
            if(params.getPriceTo()!=null){
                price.lte(params.getPriceTo());
            }
            bool.filter(price);
        }


        //遍历所有属性的组合关系生成响应的过滤条件
        if(params.getProps()!=null && params.getProps().length>0){
            //2.4）、按照前端传递的属性id：value的对应关系进行检索
            for (String prop : params.getProps()) {
                //遍历每一个属性 prop ; 2:win10-android-ios
                String[] split = prop.split(":");
                if(split!=null && split.length == 2){
                    String attrId = split[0];
                    String[] attrValues = split[1].split("-");
                    //nested里面的query条件
                    BoolQueryBuilder qb = new BoolQueryBuilder();
                    //属性id
                    TermQueryBuilder tqbAttrId = new TermQueryBuilder("attrValueList.productAttributeId",attrId);
                    qb.must(tqbAttrId);
                    //属性值
                    TermsQueryBuilder termsAttrValues = new TermsQueryBuilder("attrValueList.value",attrValues);
                    qb.must(termsAttrValues);


                    NestedQueryBuilder nested = new NestedQueryBuilder("attrValueList",qb, ScoreMode.None);
                    bool.filter(nested);
                }
            }

        }




        builder.query(bool);

        //2、分页  2  1:0,2  2:2,2  3:4,2
        builder.from((params.getPageNum()-1)*params.getPageSize());
        builder.size(params.getPageSize());

        //3、高亮
        if(!StringUtils.isEmpty(params.getKeyword())){
            //前端传递了按关键字的查询条件
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<b style='color:red'>")
                            .postTags("</b>")
                            .field("name");
            builder.highlighter(highlightBuilder);
        }

        //4、排序 0:asc  0：综合排序  1：销量  2：价格
        String order = params.getOrder();
        if(!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            //验证传递的参数
            if(split!=null && split.length == 2){
                //解析升降序规则
                SortOrder sortOrder = split[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
                if(split[0].equals("0")){
                    builder.sort("_score", sortOrder);
                }
                if(split[0].equals("1")){
                    builder.sort("sale",sortOrder);
                }
                if(split[0].equals("2")){
                    builder.sort("price",sortOrder);
                }
            }
        }


        //5、聚合
        //5.1）、属性嵌套大聚合
        NestedAggregationBuilder attrAggg = new NestedAggregationBuilder("attr_agg","attrValueList");

        //嵌套大聚合里面的子聚合
        TermsAggregationBuilder attrId_agg = new TermsAggregationBuilder("attrId_agg", ValueType.LONG);
        attrId_agg.field("attrValueList.productAttributeId");

        //子聚合里面的子聚合

        TermsAggregationBuilder attrName_agg = new TermsAggregationBuilder("attrName_agg", ValueType.STRING);
        TermsAggregationBuilder attrValue_agg = new TermsAggregationBuilder("attrValue_agg", ValueType.STRING);

        attrName_agg.field("attrValueList.name");
        attrValue_agg.field("attrValueList.value");

        //subAggregation子聚合
        attrId_agg.subAggregation(attrName_agg);
        attrId_agg.subAggregation(attrValue_agg);

        attrAggg.subAggregation(attrId_agg);
        builder.aggregation(attrAggg);


        //5.2）、品牌嵌套大聚合
        //品牌id聚合
        TermsAggregationBuilder brandAggg = new TermsAggregationBuilder("brandId_agg",ValueType.LONG);
        brandAggg.field("brandId");

        //品牌name子聚合
        TermsAggregationBuilder brandNameAgg = new TermsAggregationBuilder("brandName_agg", ValueType.STRING);
        brandNameAgg.field("brandName");

        brandAggg.subAggregation(brandNameAgg);
        builder.aggregation(brandAggg);

        //5.3）、分类嵌套大聚合
        //按照cid大聚合
        TermsAggregationBuilder categoryAggg = new TermsAggregationBuilder("catelog_agg",ValueType.LONG);
        categoryAggg.field("productCategoryId");

        //按照catename子聚合
        TermsAggregationBuilder categoryNameAggg = new TermsAggregationBuilder("catelogName_agg",ValueType.STRING);
        categoryNameAggg.field("productCategoryName");

        categoryAggg.subAggregation(categoryNameAggg);
        builder.aggregation(categoryAggg);


        return builder.toString();
    }
}
@NoArgsConstructor
@AllArgsConstructor
@Data
class User{
    private String username;
    private String email;
    private Integer age;
}
