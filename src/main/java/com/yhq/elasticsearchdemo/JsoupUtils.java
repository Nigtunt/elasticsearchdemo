package com.yhq.elasticsearchdemo;

import com.yhq.elasticsearchdemo.entity.Content;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YHQ
 * @Date: 2020/6/16 11:20
 */
public class JsoupUtils {
    public static final String url = "https://search.jd.com/Search?keyword=";


    public List<Content> get(String name) throws IOException {
        Connection connect = Jsoup.connect(url + name);

        Document x = connect.get();
        Element element = x.getElementById("J_goodsList");
        Elements li = element.select(".gl-warp>li");
        List<Content> list = new ArrayList<>();
        for (Element element1 : li) {
            String src = element1.select(".p-img img").eq(0).attr("src");
            if ("".equals(src))
                src = element1.select(".p-img img").eq(0).attr("data-lazy-img");

            String price = element1.select(".p-price strong").eq(0).text();
            String title = element1.getElementsByClass("p-name").text();
            list.add(new Content(title,src.substring(2),price.substring(1)));
        }
        return list;
    }

    public static void main(String args[]) throws IOException {
        new JsoupUtils().get("裤子").forEach(System.out::println);
    }
}
