package com.sonydafa.pj.service;

import com.google.gson.*;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ScuRequest {
    //返回还有多少条未评
    public static int submitOne(Map<String,String>cookieMap){
        int returnValue=-1;
        try{
            Map<String, List<Map>> pingjiao = pingjiao(cookieMap);
            if(pingjiao==null) return -1;
            List<Map> list = pingjiao.get("not");
            if(list==null||list.size()<1) return -1;
            Map<String, String> formData = getForm(cookieMap, list.get(0));
            String submit = submit(cookieMap, formData);
            System.out.println("提交结果:*******************************");
            System.out.println(submit);
            returnValue= pingjiao.get("not").size();
            JsonObject resp = new JsonParser().parse(submit).getAsJsonObject();
            String status=resp.get("result").getAsString();
            if(status.equals("/lagout")||status.equals("error"))
                throw new IOException("提交出错");
        }catch (IOException e){
            e.printStackTrace();
            returnValue=-1;
        }
        return returnValue;
    }
    //保存cookie及验证码
    public static Map<String,String> saveCaptcha(HttpServletRequest request, String path) throws IOException
    {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod1 = new GetMethod("http://zhjw.scu.edu.cn/img/captcha.jpg");
        //保存下载下来的验证码
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        httpClient.executeMethod(getMethod1);
        Cookie[] cookies1 = httpClient.getState().getCookies();
        Map<String,String>cookieMap=new HashMap<>();
        for (Cookie c : cookies1) {
            String[]temp=c.toString().split("=");
            cookieMap.put(temp[0],temp[1]);
        }
        path+=cookieMap.get("JSESSIONID")+".jpg";
        cookieMap.put("path",path);
        System.out.println("store file path is:"+path);
        FileOutputStream fs = new FileOutputStream(new File(path));
        InputStream is = getMethod1.getResponseBodyAsStream();
        byte[] b = new byte[1024];
        int n ;
        while ((n = is.read(b)) != -1) {
            fs.write(b, 0, n);
        }
        is.close();
        fs.close();
        return cookieMap;
    }
    //进行评教提交
    public static String submit(Map<String,String> cookie,Map<String,String> data) throws IOException
    {
        if(data==null||data.size()<1) return "";
        String url="http://zhjw.scu.edu.cn//student/teachingEvaluation/teachingEvaluation/evaluation";
        Connection.Response resp = Jsoup.connect(url).ignoreContentType(true).
                data(data).cookies(cookie).method(Connection.Method.POST).execute();
        return resp.body();
    }
    //获取单条课程记录，提交评教表单，data是返回前端的哪些数据，表示课程的信息
    public static Map<String,String> getForm(Map<String,String> cookie,Map data) throws IOException{
        if(data==null||data.size()<1) return null;
        String url="http://zhjw.scu.edu.cn/student/teachingEvaluation/teachingEvaluation/evaluationPage";

        Document document = Jsoup.connect(url).cookies(cookie).ignoreContentType(true).data(data).post();
        Elements select = document.select(".widget-content form input");
        Map<String,String> form=new HashMap<>();
        Iterator<Element> iterator = select.iterator();
        while (iterator.hasNext()){
            Element tmp=iterator.next();
            String name=tmp.attr("name");
            String value=tmp.attr("value");
            switch (name){
                case "tokenValue":
                case "questionnaireCode":
                case "evaluationContentNumber":
                case "evaluatedPeopleNumber":
                case "count":
                    form.put(name,value);
            }
        }
        Elements table = document.select("table tbody tr td div label input");
        Iterator<Element> iterator1 = table.iterator();
        while (iterator1.hasNext()){
            Element tmp=iterator1.next();
            String name=tmp.attr("name");
            String value=tmp.attr("value");
            if(value.equals("10_1"))
                form.put(name,value);
        }
        String[]comments=new String[]{"老师授课的方式非常适合我们，他根据本课程知识结构的特点，重点突出，层次分明。理论和实际相结合，通过例题使知识更条理化。",
        "老师授课有条理，有重点，对同学既热情又严格，是各位老师学习的榜样。",
                "老师对待教学认真负责，语言生动，条理清晰，举例充分恰当，对待学生严格要求，能够鼓励学生踊跃发言，使课堂气氛比较积极热烈。",
                "课堂内容充实，简单明了，使学生能够轻轻松松掌握知识。",
                "教学内容丰富有效，教学过程中尊重学生，有时还有些洋幽默，很受同学欢迎。",
                "老师人很好"
        };
        String pickComment=comments[(int)(Math.random()*6)];
        form.put("zgpj",pickComment);

        return form;

    }
    //所有的评估数据
    public static Map<String,List<Map>> pingjiao(Map<String,String> cookies) throws IOException {
        String url="http://zhjw.scu.edu.cn/student/teachingEvaluation/teachingEvaluation/search";
        Connection conn= Jsoup.connect(url);
        Connection.Response response = conn.ignoreContentType(true).cookies(cookies).method(Connection.Method.POST).execute();
        String data=response.body();
        //System.out.println("resp data:"+data);
        //if(data.charAt('0')!='{') return null;
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(data);
        }catch (Exception e){
            return null;
        }
        JsonArray data1 = jsonElement.getAsJsonObject().get("data").getAsJsonArray();
        Map<String,List<Map>> result=new HashMap<>();

        List<Map> yet=new LinkedList<>();
        List<Map> not=new LinkedList<>();
        for(JsonElement tmp:data1){
            Map<String,String> tempMap=new HashMap<>();
            //System.out.println("请求结果：\n"+tmp);
            String isEvaluated=tmp.getAsJsonObject().get("isEvaluated").getAsString();
            String courseName=tmp.getAsJsonObject().get("evaluationContent").getAsString();
            String evaluatedPeople=tmp.getAsJsonObject().get("evaluatedPeople").getAsString();
            JsonObject id=tmp.getAsJsonObject().get("id").getAsJsonObject();
            String evaluatedPeopleNumber=id.get("evaluatedPeople").getAsString();
            JsonObject questionnaire=tmp.getAsJsonObject().get("questionnaire").getAsJsonObject();
            String questionnaireCode=questionnaire.get("questionnaireNumber").getAsString();
            String questionnaireName=questionnaire.get("questionnaireName").getAsString();
            String evaluationContentNumber=id.get("evaluationContentNumber").getAsString();
            String evaluationContentContent="";//id.get("evaluationContent").getAsString();
            tempMap.put("courseName",courseName);
            tempMap.put("evaluatedPeople",evaluatedPeople);
            tempMap.put("evaluatedPeopleNumber",evaluatedPeopleNumber);
            tempMap.put("questionnaireCode",questionnaireCode);
            tempMap.put("questionnaireName",questionnaireName);
            tempMap.put("evaluationContentNumber",evaluationContentNumber);
            tempMap.put("evaluationContentContent",evaluationContentContent);
            if(isEvaluated.equals("是"))
                yet.add(tempMap);
            else
                not.add(tempMap);
        }
        result.put("not",not);
        result.put("yet",yet);
        return result;

    }
}
