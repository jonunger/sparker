package com.jonunger.examples.spark;

import com.jonunger.examples.spark.model.CourseIdea;
import com.jonunger.examples.spark.model.CourseIdeaDAO;
import com.jonunger.examples.spark.model.NotFoundException;
import com.jonunger.examples.spark.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by junger on 1/16/2017.
 */
public class Sparker {

    private static String getFlashMessage(Request req) {
        if (req.session(false) == null) {
            return null;
        }
        if (!req.session().attributes().contains(FLASH_MESSAGE_KEY)) {
            return null;
        }
        return (String) req.session().attribute(FLASH_MESSAGE_KEY);
    }

    private static final String FLASH_MESSAGE_KEY = "flash_message";

    public static void main(String[] args) {

        staticFileLocation("/public");

        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();

        before("/ideas", (req, res) -> {
            if (req.cookie("username") == null) {
                res.redirect("/");
                halt();
            }
        });

        get("/hello", (req, res) -> {
            Map<String, String> model = new HashMap<>();
            model.put("username", req.cookie("username"));
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        post("/sign-in", (req, res) -> {
            String username = req.queryParams("username");
            res.cookie("username", username);
            Map<String, String> model = new HashMap<>();
            model.put("username", username);
            return new ModelAndView(model, "sign-in.hbs");
        }, new HandlebarsTemplateEngine());

        get("/ideas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());
            model.put("flashMessage", getFlashMessage(req));
            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

        get("/ideas/:slug", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("idea", dao.findBySlug(req.params("slug")));
            return new ModelAndView(model, "idea.hbs");
        }, new HandlebarsTemplateEngine());

        post("/ideas", (req, res) -> {
            String title = req.queryParams("title");
            CourseIdea courseIdea = new CourseIdea(title, req.cookie("username"));
            dao.add(courseIdea);
            res.redirect("/ideas");
            return null;
        }, new HandlebarsTemplateEngine());

        post("/ideas/:slug/vote", (req, res) -> {
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            boolean added = idea.addVoter(req.attribute("username"));
            if (added) {
                setFlashMessage(req, "Thanks for your vote!");
            }
            res.redirect("/ideas");
            return null;
        });

        exception(NotFoundException.class, (exec, req, res) -> {
            res.status(404);
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
            String html = engine.render(new ModelAndView(null, "not-found.hbs"));
            res.body(html);
        });
    }

    private static void setFlashMessage(Request req, String message) {
        req.session().attribute(FLASH_MESSAGE_KEY, message);
    }
}
