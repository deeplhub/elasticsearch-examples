package com.xh.es.test;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.xh.es.model.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ES 文档
 *
 * @author H.Yang
 * @date 2022/11/22
 */
@Slf4j
@RunWith(SpringRunner.class)
public class ElasticSearchDocTest {

    private RestHighLevelClient restClient = null;

    @Before
    public void connection() {
        log.info("创建ES客户端...");
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "elastic"));

        restClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("172.16.1.100", 9200, HttpHost.DEFAULT_SCHEME_NAME)
                ).setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(credentialsProvider))
        );


    }

    @After
    public void close() throws IOException {
        if (restClient == null) {
            throw new NullPointerException("ES连接为空");
        }

        log.info("关闭ES客户端...");
        restClient.close();
    }

    /**
     * 创建文档
     *
     * @throws IOException
     */
    @Test
    public void createDoc() throws IOException {

        UserEntity user = UserEntity.builder().name("zhangsan").age(30).sex("男").build();

        IndexRequest request = new IndexRequest();

        request.index("user").id("1001");
        request.source(JSONUtil.toJsonStr(user), XContentType.JSON);

        IndexResponse response = restClient.index(request, RequestOptions.DEFAULT);

        log.info("Result : {}", response.getResult());
        log.info("Index : {}", response.getIndex());
    }


    /**
     * 修改文档
     *
     * @throws IOException
     */
    @Test
    public void modifyDoc() throws IOException {
        UpdateRequest request = new UpdateRequest();

        request.index("user").id("1001");
        request.doc(XContentType.JSON, "sex", "女");

        UpdateResponse response = restClient.update(request, RequestOptions.DEFAULT);

        log.info("Result：{}", response.getResult());
    }

    /**
     * 查询文档
     *
     * @throws IOException
     */
    @Test
    public void getDoc() throws IOException {
        GetRequest request = new GetRequest();

        request.index("user").id("1001");

        GetResponse response = restClient.get(request, RequestOptions.DEFAULT);

        log.info("查询文档：{}", response.getSourceAsString());
    }

    /**
     * 删除文档
     *
     * @throws IOException
     */
    @Test
    public void deleteDoc() throws IOException {
        DeleteRequest request = new DeleteRequest();

        request.index("user").id("1001");

        DeleteResponse response = restClient.delete(request, RequestOptions.DEFAULT);

        log.info("查询文档：{}", response.toString());
    }


    /**
     * 批量创建文档
     *
     * @throws IOException
     */
    @Test
    public void createBatchDoc() throws IOException {
        BulkRequest request = new BulkRequest();

        request.add(new IndexRequest().index("user1").id("1001").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z1").age(30).sex("男").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1002").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z2").age(20).sex("男").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1003").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z3").age(21).sex("女").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1004").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z4").age(23).sex("女").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1005").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z5").age(22).sex("男").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1006").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z6").age(12).sex("女").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1007").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z7").age(30).sex("男").build()), XContentType.JSON));
        request.add(new IndexRequest().index("user1").id("1008").source(JSONUtil.toJsonStr(UserEntity.builder().name("Z8").age(43).sex("女").build()), XContentType.JSON));


        BulkResponse response = restClient.bulk(request, RequestOptions.DEFAULT);

        log.info("花费时间 : {}", response.getTook());
        log.info("响应 : {}", response.getItems());

    }

    /**
     * 批量删除文档
     *
     * @throws IOException
     */
    @Test
    public void deleteBatchDoc() throws IOException {
        BulkRequest request = new BulkRequest();

        request.add(new DeleteRequest().index("user").id("1001"));
        request.add(new DeleteRequest().index("user").id("1002"));
        request.add(new DeleteRequest().index("user").id("1003"));
        request.add(new DeleteRequest().index("user").id("1004"));
        request.add(new DeleteRequest().index("user").id("1005"));
        request.add(new DeleteRequest().index("user").id("1006"));
        request.add(new DeleteRequest().index("user").id("1007"));
        request.add(new DeleteRequest().index("user").id("1008"));


        BulkResponse response = restClient.bulk(request, RequestOptions.DEFAULT);

        log.info("花费时间 : {}", response.getTook());
        log.info("响应 : {}", JSONUtil.toJsonStr(response.getItems()));

    }


    /**
     * 查询索引中全部数据
     *
     * @throws IOException
     */
    @Test
    public void queryAll() throws IOException {
        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }

    /**
     * 条件查询
     *
     * @throws IOException
     */
    @Test
    public void termQuery() throws IOException {
        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("sex", "男")));

        // TODO 如果按名称查询需要加上 keyword，否则查不出数据
//        request.source(new SearchSourceBuilder().query(QueryBuilders.termQuery("name.keyword", "Z3")));

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }


    /**
     * 分页查询
     *
     * @throws IOException
     */
    @Test
    public void pageQuery() throws IOException {

        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());

        // (当前页码-1)*每页显示的数据条数
        builder.from(2);
        builder.size(2);

        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(builder);

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }

    /**
     * 排序查询
     *
     * @throws IOException
     */
    @Test
    public void sortQuery() throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());

        builder.sort("age", SortOrder.DESC);

        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(builder);

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }

    /**
     * 过滤字段查询
     *
     * @throws IOException
     */
    @Test
    public void filterQuery() throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());

        String[] includes = {}, // 包含
                excludes = {"name"};// 排除
        builder.fetchSource(includes, excludes);

        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(builder);

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }

    /**
     * 组合查询
     *
     * @throws IOException
     */
    @Test
    public void matchQuery() throws IOException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //  and
//        boolQuery.must(QueryBuilders.matchQuery("age", 23));
//        boolQuery.must(QueryBuilders.matchQuery("sex", "女"));
//        boolQuery.mustNot(QueryBuilders.matchQuery("sex", "女"));

        // or
        boolQuery.should(QueryBuilders.matchQuery("age", 23));
        boolQuery.should(QueryBuilders.matchQuery("age", 30));

        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(new SearchSourceBuilder().query(boolQuery));

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }

    /**
     * 范围查询
     *
     * @throws IOException
     */
    @Test
    public void rangeQuery() throws IOException {
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age");

        rangeQuery.gte(20);
        rangeQuery.lte(30);

        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(new SearchSourceBuilder().query(rangeQuery));

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }

    }

    /**
     * 模糊查询
     *
     * @throws IOException
     */
    @Test
    public void fuzzyQuery() throws IOException {
        SearchRequest request = new SearchRequest();

        request.indices("user");
        // fuzzy query用法和match基本一致，参数也包含fuzziness、prefix_length、max_expansions、transpositions，唯一的不同点是Fuzzy query的查询不分词。
        request.source(new SearchSourceBuilder().query(QueryBuilders.fuzzyQuery("name.keyword", "Z").fuzziness(Fuzziness.ONE)));

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }
    }

    /**
     * 高亮查询
     *
     * @throws IOException
     */
    @Test
    public void highlightQuery() throws IOException {
        SearchRequest request = new SearchRequest();

        request.indices("user");
        request.source(new SearchSourceBuilder()
                .query(QueryBuilders.termQuery("sex", "男"))
                .highlighter(new HighlightBuilder()
                        .preTags("<font color='red'>")
                        .postTags("</font>")
                        .field("sex")
                )
        );

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }
    }

    /**
     * 聚合查询
     *
     * @throws IOException
     */
    @Test
    public void aggregationQuery() throws IOException {
        SearchRequest request = new SearchRequest();

        request.indices("user");
//        request.source(new SearchSourceBuilder().aggregation(AggregationBuilders.max("maxAge").field("age")) );
        request.source(new SearchSourceBuilder().aggregation(AggregationBuilders.terms("ageGroup").field("age")));

        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

        // 获取匹配的数据
        SearchHits hits = response.getHits();

        log.info("获取总共条数：{}", hits.getTotalHits());
        log.info("查询时间：{}", response.getTook());

        for (SearchHit hit : hits) {
            log.info("打印数据：{}", hit.getSourceAsString());
        }
    }


//    /**
//     * 多条件查询
//     *
//     * @throws IOException
//     */
//    @Test
//    public void demo() throws IOException {
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        //  and
////        boolQueryBuilder.must(QueryBuilders.matchQuery("age", 23));
////        boolQueryBuilder.must(QueryBuilders.matchQuery("sex", "女"));
////        boolQueryBuilder.mustNot(QueryBuilders.matchQuery("sex", "女"));
//        // or
////        boolQueryBuilder.should(QueryBuilders.matchQuery("age", 23));
////        boolQueryBuilder.should(QueryBuilders.matchQuery("age", 30));
//
//        // set = '女' and age >=30 and age <= 50
////        boolQueryBuilder.must(QueryBuilders.termQuery("sex", "女"));
////        boolQueryBuilder.must(QueryBuilders.rangeQuery("age").gte(30).lte(50));
//
//
//        SearchRequest request = new SearchRequest("user1");
////        request.source(new SearchSourceBuilder().query(boolQueryBuilder));
//
////        SearchSourceBuilder searchSourceBuilder = this.doTermQueryLogic(new SearchSourceBuilder(), boolQueryBuilder, "age", 23, ElasticSearchConditionEnum.AND, null);
////        searchSourceBuilder = this.doTermQueryLogic(searchSourceBuilder, boolQueryBuilder, "sex", "男", ElasticSearchConditionEnum.OR, null);
//
//        List<Map<String, Object>> list = new ArrayList<>();
//        Map<String, Object> map = new HashMap<>();
//
////        map.put("k", "age");
////        map.put("v", 23);
////        map.put("enum", ElasticSearchConditionEnum.AND);
////        list.add(map);
////
////        map = new HashMap<>();
////        map.put("k", "name.keyword");
////        map.put("v", "Z4");
////        map.put("enum", ElasticSearchConditionEnum.AND);
////        list.add(map);
//
//        map = new HashMap<>();
//
////        map.put("k", "sex");
////        map.put("v", "女");
////        map.put("enum", ConditionEnum.AND);
////        list.add(map);
//
//        map = new HashMap<>();
//        map.put("k", "age");
//        map.put("v", 30);
//        map.put("conditionName", "gte");
//        list.add(map);
//
//        map = new HashMap<>();
//        map.put("k", "age");
//        map.put("v", 50);
//        map.put("conditionName", "lte");
//        list.add(map);
//
//        SearchSourceBuilder searchSourceBuilder = this.doRangeQueryLogic(list, null);
//        request.source(searchSourceBuilder);
//
//        log.info("请求报文：{}", request);
//        SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
//        log.info("响应报文：{}", response);
//
//        // 获取匹配的数据
//        SearchHits hits = response.getHits();
//
//        log.info("获取总共条数：{}", hits.getTotalHits());
//        log.info("查询时间：{}", response.getTook());
//
//        for (SearchHit hit : hits) {
//            log.info("打印数据：{}", hit.getSourceAsString());
//        }
//
//
//    }
//
//
//    public SearchSourceBuilder doTermQueryLogic(SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, String name, Object value, ConditionEnum conditionEnum, Map<String, SortOrder> sortOrderMap) {
//        conditionEnum.getBoolQueryBuilder(boolQueryBuilder, QueryBuilders.termQuery(name, value));
//
//        // 拼接排序
//        if (MapUtil.isNotEmpty(sortOrderMap)) {
//            sortOrderMap.forEach(searchSourceBuilder::sort);
//        }
//        searchSourceBuilder = searchSourceBuilder.query(boolQueryBuilder);
//        return searchSourceBuilder;
//    }
//
//    public SearchSourceBuilder doTermQueryLogic(List<Map<String, Object>> list, Map<String, SortOrder> sortOrderMap) {
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        list.stream().forEach(o -> {
//            ConditionEnum conditionEnum = (ConditionEnum) o.get("enum");
//            conditionEnum.getBoolQueryBuilder(boolQueryBuilder, QueryTypeEnum.TERM_QUERY.getQueryBuilder(o.get("k") + "", o.get("v") + ""));
//            System.out.println();
//        });
//
//
//        // 拼接排序
//        if (MapUtil.isNotEmpty(sortOrderMap)) {
//            sortOrderMap.forEach(searchSourceBuilder::sort);
//        }
//        searchSourceBuilder = searchSourceBuilder.query(boolQueryBuilder);
//        return searchSourceBuilder;
//    }
//
//    public SearchSourceBuilder doMatchQueryLogic(List<Map<String, Object>> list, Map<String, SortOrder> sortOrderMap) {
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        list.stream().forEach(o -> {
//            ConditionEnum conditionEnum = (ConditionEnum) o.get("enum");
//            conditionEnum.getBoolQueryBuilder(boolQueryBuilder, QueryTypeEnum.MATCH_QUERY.getQueryBuilder(o.get("k") + "", o.get("v") + ""));
//            System.out.println();
//        });
//
//
//        // 拼接排序
//        if (MapUtil.isNotEmpty(sortOrderMap)) {
//            sortOrderMap.forEach(searchSourceBuilder::sort);
//        }
//        searchSourceBuilder = searchSourceBuilder.query(boolQueryBuilder);
//        return searchSourceBuilder;
//    }
//
//    public SearchSourceBuilder doRangeQueryLogic(List<Map<String, Object>> list, Map<String, SortOrder> sortOrderMap) {
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        RangeQueryBuilder rangeQuery = null;
//        for (Map<String, Object> o : list) {
//            if (rangeQuery == null) {
//                rangeQuery = QueryTypeEnum.RANGE_QUERY.getQueryBuilder(o.get("k") + "", null);
//            }
//
//            Class<RangeQueryBuilder> clazz = RangeQueryBuilder.class;
//            try {
//                Method method = clazz.getDeclaredMethod(o.get("conditionName") + "", Object.class);
//
//                method.invoke(rangeQuery, o.get("v"));
//            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        // 拼接排序
//        if (MapUtil.isNotEmpty(sortOrderMap)) {
//            sortOrderMap.forEach(searchSourceBuilder::sort);
//        }
//
//        searchSourceBuilder = searchSourceBuilder.query(rangeQuery);
//        return searchSourceBuilder;
//    }
//
//
//    public static void main(String[] args) {
//        DemoEnum test = DemoEnum.TEST;
//
//        DemoEnum.TEST.test();
//        DemoEnum.TEST.name();
//
//        DemoEnum.TEST.test();
//
//        QueryInstanceofStrategy strategy = DemoEnum.TEST.test();
//
//
//    }
}