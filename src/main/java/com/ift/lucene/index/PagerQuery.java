package com.ift.lucene.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

/**
 * @author ift
 * @create 2018-09-22 17:45
 * @desc 分页查询
 */
public class PagerQuery {

    public static void main(String[] args) throws Exception{
        pagerSearch(1, 2);
    }

    public static void pagerSearch(int pageIndex, int pageSize) throws Exception{
        Directory directory = FSDirectory.open(Paths.get("E:\\Lucene_db\\article_tb"));

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Analyzer analyzer = new SmartChineseAnalyzer();
        QueryParser queryParser = new MultiFieldQueryParser(new  String[]{"title"}, analyzer);
        Query query = queryParser.parse("title:广州 OR content:广州");

        Formatter formatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
        Scorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);

        Fragmenter fragmenter = new SimpleFragmenter(10);

        highlighter.setTextFragmenter(fragmenter);

        TopDocs topDocs = indexSearcher.search(query, 10);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        // 1 2 0 2
        // 2 2 2 4
        int startIndex = (pageIndex - 1) * pageSize;
        int endIndex = pageIndex * pageSize > scoreDocs.length ? scoreDocs.length : pageIndex * pageSize;
        for (int i = startIndex; i < endIndex; i++) {
            int docId = scoreDocs[i].doc;
            Document document = indexReader.document(docId);
            String id  = document.get("id");
            String title = document.get("title");
            String content = document.get("content");
            System.out.println("高亮前： id: " + id + ", title: " + title + ", content: " +  content);
            String titleEnd = highlighter.getBestFragment(analyzer, "title", title);
            String contentEnd = highlighter.getBestFragment(analyzer, "content", content);
            System.out.println("高亮后： id: " + id + ", title: " + titleEnd + ", content: " + contentEnd);
        }
    }
}
