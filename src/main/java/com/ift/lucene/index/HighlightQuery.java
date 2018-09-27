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
 * @create 2018-09-22 16:55
 * @desc 高亮显示
 */
public class HighlightQuery {

    public static void main(String[] args) throws Exception{
        searchIndex();
    }

    public static void searchIndex() throws Exception{
        Directory directory = FSDirectory.open(Paths.get("E:\\Lucene_db\\article_tb"));
        IndexReader indexReader = DirectoryReader.open(directory);

        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Analyzer analyzer = new SmartChineseAnalyzer();
        QueryParser queryParser = new MultiFieldQueryParser(new String[]{"title"}, analyzer);
        Query query = queryParser.parse("title:广州 OR content:广州");

        /**************************************高亮显示******************************************/
        Formatter formatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
        Scorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        //设置最多只取多少个字
        Fragmenter fragmenter = new SimpleFragmenter(10);
        highlighter.setTextFragmenter(fragmenter);
        /***************************************************************************************/
        TopDocs topDocs = indexSearcher.search(query, 10);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docId = scoreDoc.doc;
            Document document = indexReader.document(docId);
            String id = document.get("id");
            String title = document.get("title");
            String content = document.get("content");
            System.out.println("高亮前：id: " + id + ", title: " + title + ", content: " + content);
            //高亮后
            String titleEnd = highlighter.getBestFragment(analyzer, "title", title);
            String contentEnd = highlighter.getBestFragment(analyzer, "content", content);
            System.out.println("高亮后： id: " + id + ", title: " + titleEnd + ", content: " + contentEnd);
        }
    }
}
