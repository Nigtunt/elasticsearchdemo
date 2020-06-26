package com.yhq.elasticsearchdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhq.elasticsearchdemo.entity.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@SpringBootTest
class ElasticsearchdemoApplicationTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    RestHighLevelClient client;

    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    void createClient() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("yhq_index");

        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * 获得索引
     */
    @Test
    void getClient() throws IOException {
        GetIndexRequest request = new GetIndexRequest("yhq_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        if (exists){
            System.out.println("存在");

            GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);

            String[] indices = getIndexResponse.getIndices();
            System.out.println(Arrays.deepToString(indices));
        }
    }
    /**
     * 删除索引
     */
    @Test
    void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("yhq_index");
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);

        System.out.println(delete.isAcknowledged());
    }

    /**
     * 添加文档
     */
    @Test
    void testAdddocument() throws IOException {
        User user = new User("yhq",16);
        IndexRequest request = new IndexRequest("yhq_index");

        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");

        request.source(new ObjectMapper().writeValueAsString(user), XContentType.JSON);

        IndexResponse response = client.index(request,RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }
    @Test
    void isExists() throws IOException {
        GetRequest request = new GetRequest("yhq_index","1");

        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        System.out.println(client.exists(request,RequestOptions.DEFAULT));

    }
    /**
     * 获取文档
     */

    @Test
    void getDocument() throws IOException {
        GetRequest request = new GetRequest("yhq_index","1");

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        System.out.println(response.getSourceAsString());
        System.out.println(response);
    }
    /**
     * 更新文档
     */
    @Test
    void updateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("yhq_index","1");
        User u = new User("yhq",20);
        request.doc(new ObjectMapper().writeValueAsString(u),XContentType.JSON);
        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        System.out.println(update.getGetResult());
        System.out.println(update.toString());
    }
    /**
     * 删除文档
     */
    @Test
    void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("yhq_index","1");

        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    // 批量插入数据
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout("10s");
        ObjectMapper mapper = new ObjectMapper();

        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new User("yhq"+i,i));
        }
        for (int i = 0; i < list.size(); i++) {
            request.add(
                    new IndexRequest("yhq_index")
//                    .id(""+(i+1))
                    .source(mapper.writeValueAsString(list.get(i)),XContentType.JSON)
            );
        }

        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);



        //查询数据    System.out.println(bulk.status());
        System.out.println(bulk.hasFailures());
    }

    //查询数据
    @Test
    public void searchTest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("yhq_index");

        SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
        searchBuilder.highlighter(new HighlightBuilder().preTags("<test>").postTags("</test>"));
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "yhq3");
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "yhq1");

        searchBuilder.query(matchQueryBuilder);

        searchRequest.source(searchBuilder);

        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : search.getHits().getHits()) {
            System.out.print(hit.getScore());
            System.out.println(hit.getSourceAsString());
        }



    }
}
