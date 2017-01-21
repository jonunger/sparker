package com.jonunger.treehouse.spark.model;

import java.util.List;

/**
 * Created by junger on 1/16/2017.
 */
public interface CourseIdeaDAO {
    boolean add(CourseIdea idea);

    List<CourseIdea> findAll();

    CourseIdea findBySlug(String slug);
}
