package com.example.awesomecurriculum.utils.school;

import android.util.Log;

import com.example.awesomecurriculum.data.model.Course;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param {String} 学号
 * @param {String} 密码
 * @author Jacob
 * @Deprecated 自动获取上海大学课表
 * @return List
 */
public class SHU {
    private static String[] colorList = new String[]{"996633", "CC3333", "993333", "990033", "CC9999", "666699", "6699FF", "669933", "660033", "336666"};

    public static List<Course> getCourse(String number, String psd) {
        List<List> course = new ArrayList<>();
        try {
            Document doc;
            Connection.Response connect = Jsoup.connect("https://oauth.shu.edu.cn/oauth/authorize?response_type=code&client_id=yRQLJfUsx326fSeKNUCtooKw&redirect_uri=http://cj.shu.edu.cn/passport/return&state=").execute();

            Map<String, String> user_password = new HashMap<String, String>();
            user_password.put("username", number);
            user_password.put("password", psd);
            user_password.put("login_submit", "登录/Login");

            Connection.Response connect1 = Jsoup.connect("https://oauth.shu.edu.cn/login")
                    .data(user_password)
                    .cookies(connect.cookies()).followRedirects(false)
                    .method(Connection.Method.POST).timeout(10000).execute();
            Connection.Response connect2 = Jsoup.connect("https://oauth.shu.edu.cn/oauth/authorize")
                    .cookies(connect.cookies()).followRedirects(false)
                    .method(Connection.Method.GET).timeout(10000).execute();
            Connection.Response connect3 = Jsoup.connect(connect2.header("location"))
                    .cookies(connect.cookies()).followRedirects(false)
                    .method(Connection.Method.GET).timeout(10000).execute();

            doc = Jsoup.connect("http://cj.shu.edu.cn/StudentPortal/StudentSchedule")
                    .data("studentNo", number)
                    .cookies(connect3.cookies())
                    .post();
            Elements term = doc.body().select("option");
            String academicTermID = term.get(5).attr("value");
            Element coursePage = Jsoup.connect("http://cj.shu.edu.cn/StudentPortal/CtrlStudentSchedule")
                    .data("academicTermID", academicTermID)
                    .cookies(connect3.cookies())
                    .post()
                    .body();
            Elements ele2 = coursePage.select("tr");
            for (int i = 2; i < ele2.size(); i++) {
                Elements items = ele2.get(i).getElementsByTag("td");
                if (items.size() < 8) {
                    continue;
                }
                List<String> time = new ArrayList<>();
                time.add(items.get(0).text());
                time.add(items.get(1).text());
                time.add(items.get(2).text());
                time.add(items.get(3).text());
                time.add(items.get(5).text());
                time.addAll(getInformations(items.get(4).text()));
                course.add(time);
                Log.d("courseTime", course.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return change2Course(course);
    }

    private static List<Course> change2Course(List<List> course) {
//        Course[] res = new Course[course.size()];
        List<Course> res = new ArrayList<>();
        int index = 0;
        for (List s : course) {
            for (int i = 5; i < s.size(); i++) {
                int week = 0;
                switch (s.get(i).toString().charAt(0)) {
                    case '一':
                        week = 1;
                        break;
                    case '二':
                        week = 2;
                        break;
                    case '三':
                        week = 3;
                        break;
                    case '四':
                        week = 4;
                        break;
                    case '五':
                        week = 5;
                        break;
                    case '六':
                        week = 6;
                        break;
                    case '日':
                        week = 7;
                        break;
                    default:
                        week = 0;
                        break;
                }

                int timeStart = Integer.parseInt(s.get(i).toString().substring(1, s.get(i).toString().indexOf("-")));
                int timeEnd = Integer.parseInt(s.get(i).toString().substring(s.get(i).toString().indexOf("-") + 1));
                Course courseItem = new Course(s.get(1).toString(), s.get(3).toString(), s.get(4).toString(), week, timeStart - 1, timeEnd - 1, colorList[index % colorList.length], 0, s.get(0).toString());
                Log.d("courseTime", courseItem.toString());
                res.add(courseItem);
            }
            index++;
        }
        return res;
    }

    public static List<String> getInformations(String info) {
        List<String> strList = java.util.Arrays.asList(info.split(" "));
        List<String> list = new ArrayList<>();
        String regex = "[一二三四五][0-9]+-[0-9]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(info);
        List<String> courseTime = new ArrayList<>();
        while (matcher.find()) {
            courseTime.add(matcher.group());
        }
        list.addAll(courseTime);
        return list;
    }
}
