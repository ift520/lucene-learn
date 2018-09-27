package com.ift.lucene.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author ift
 * @create 2018-09-09 19:06
 * @desc 查询多个字段
 */
public class MultiQueryParser {

    public static void main(String[] args) {
//        addIndex();
        searchIndex();
    }

    public static void addIndex() {
        try {
            //1.创建Directory
            Directory directory = FSDirectory.open(Paths.get("E:\\Lucene_db\\article_tb"));
            //创建分词器,StandardAnalyzer:单字分词器
//            Analyzer analyzer = new StandardAnalyzer();
            Analyzer analyzer = new SmartChineseAnalyzer();
            //2.创建IndexWriterConfig配置IndexWriter信息
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            //3.创建Document，一个Document对应一条记录
            /*
             * Field.Store:
             *    YES: 会存储数据
             *    NO: 不会存储数据，但会分词，对索引库无影响
             */
            Document document = new Document();
            document.add(new StringField("id", "003", Field.Store.YES));
            document.add(new TextField("title", "好玩的地方", Field.Store.YES));
            document.add(new TextField("content", "广州白云山", Field.Store.YES));
            //4.添加Document到IndexWriter
            indexWriter.addDocument(document);
            //5.提交
            indexWriter.commit();
            //6.关闭流
            indexWriter.close();
            System.out.println("创建索引成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询索引
     */
    public static void searchIndex() {
        try {
            Directory directory = FSDirectory.open(Paths.get("E:\\Lucene_db\\article_tb"));
            //创建IndexReader
            IndexReader indexReader = DirectoryReader.open(directory);
            //创建IndexSearcher实例，通过IndexSearcher进行全文检索
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            //Query封装检索信息，第二个参数指定最多返回多少条记录
            //TermQuery不会对关键字进行分词
//            Query query = new TermQuery(new Term("title", "傻"));

            /*
             * 若要对查询字段进行分词则使用QueryParser构建查询
             */
//            Analyzer analyzer = new SmartChineseAnalyzer();
////            QueryParser queryParser = new QueryParser("title", analyzer);
////            Query query = queryParser.parse("广州");

            /*
             * 查询多个字段
             */
            Analyzer analyzer = new SmartChineseAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(new String[]{"title"}, analyzer);
            /*=========================条件过滤===============================*/
//            Query query = queryParser.parse("广州");
            //1.或：广州 地方，广州 OR 地方
//            Query query = queryParser.parse("广州 or 广东");
            //2.并且: 广州 AND 广东【AND必须大写】, +广州 +广东
//            Query query = queryParser.parse("广州 AND content:广州");
            //3.NOT：title:广州 AND NOT content:广州，title:广州 AND -content:广州|白云山
            Query query = queryParser.parse("title:广州 AND -content:广州 白云山");
            /*=========================条件过滤===============================*/
            //查询到的结果，此时查询的是索引库
            TopDocs topDocs = indexSearcher.search(query, 10);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            System.out.println("查询到的记录：" + scoreDocs.length);
            //遍历查询到的索引信息
            for (ScoreDoc scoreDoc : scoreDocs) {
                //获取文档id
                int docId = scoreDoc.doc;
                //获取文档分数
                float score = scoreDoc.score;
                System.out.println("文档id：" + docId + " 分数：" + score);
                //通过文档id去查询文档信息【实际保存的数据】
                //一个Document对应一条数据库的记录
                Document document = indexReader.document(docId);
                String id = document.get("id");
                String title = document.get("title");
                String content = document.get("content");
                System.out.println("id: " + id + " title:" + title + " content:" + content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
