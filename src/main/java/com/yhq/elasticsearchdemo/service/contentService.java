package com.yhq.elasticsearchdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhq.elasticsearchdemo.JsoupUtils;
import com.yhq.elasticsearchdemo.entity.Content;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: YHQ
 * @Date: 2020/6/16 12:24
 */
@Service
public class contentService {
    @Autowired
    @Qualifier("restHighLevelClient")
    RestHighLevelClient client;

    public boolean parseContent(String keys) throws IOException {
        List<Content> contents = new JsoupUtils().get(keys);
        BulkRequest request = new BulkRequest();
        request.timeout("10s");
        ObjectMapper mapper = new ObjectMapper();
        contents.forEach(e -> {
            try {
                System.out.println(mapper.writeValueAsString(e));
                request.add(
                        new IndexRequest("jd_goods")
                        .source(mapper.writeValueAsString(e), XContentType.JSON)
                );
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
        });

        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    public List<Map<String,Object>> search(String key,int pageNo,int pageSize) throws IOException {
        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("title",key));
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);
        request.source(searchSourceBuilder);
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits) {
            list.add(hit.getSourceAsMap());
        }
        return list;
    }

    public List<Map<String,Object>> searchHighLight(String key,int pageNo,int pageSize) throws IOException {
        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title",key));
//        searchSourceBuilder.from(pageNo);
//        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.highlighter(new HighlightBuilder()
                .requireFieldMatch(false)
                .field("title")
                .preTags("<font color='red'>")
                .postTags("</font>"));

        request.source(searchSourceBuilder);
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit : hits) {
            Map<String, HighlightField> highlightFields =
                    hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Text[] fragments = title.fragments();
            StringBuilder sb = new StringBuilder();
            for (Text fragment : fragments) {
                sb.append(fragment.string());
            }
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            sourceAsMap.put("title",sb.toString());
            list.add(sourceAsMap);

        }
        return list;
    }
}
