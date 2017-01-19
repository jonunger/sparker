package com.jonunger.treehouse.spark;

import com.jonunger.treehouse.spark.model.CourseIdea;
import com.jonunger.treehouse.spark.model.CourseIdeaDAO;
import com.jonunger.treehouse.spark.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by junger on 1/16/2017.
 */
public class Main {
    public static void main(String[] args) {

        staticFileLocation("/public");

        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();

        before("/ideas",(req, res) -> {
            if (req.cookie("username") == null){
                res.redirect("/");
                halt();
            }
        });

        get("/hello", (req, res) ->{
            Map<String, String> model = new HashMap<>();
            model.put("username", req.cookie("username"));
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        post("/sign-in", (req, res) ->{
                    String username = req.queryParams("username");
                    res.cookie("username", username);
                    Map<String, String> model = new HashMap<>();
                    model.put("username", username);
                    return new ModelAndView(model, "sign-in.hbs");
                }, new HandlebarsTemplateEngine());

        get("/ideas", (req, res) ->{
            Map<String, Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());
            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas", (req, res) ->{
            String title = req.queryParams("title");
           CourseIdea courseIdea = new CourseIdea(title, req.cookie("username"));
            dao.add(courseIdea);
            res.redirect("/ideas");
            return null;
        }, new HandlebarsTemplateEngine());
    }
}