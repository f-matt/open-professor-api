package com.github.fmatt.openprofessor.dto;

import com.github.fmatt.openprofessor.model.Course;

public class CourseSectionDto {

    private Course course;

    private Integer section;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Integer getSection() {
        return section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }

}
