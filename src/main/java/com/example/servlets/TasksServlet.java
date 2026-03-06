package com.example.servlets;

import com.example.dao.TaskDAO;
import com.example.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {
    private TaskDAO dao;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        dao = new TaskDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        if (pathInfo == null || "/".equals(pathInfo)) {
            List<Task> tasks = dao.getAll();
            resp.getWriter().write(gson.toJson(tasks));
        } else {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Task t = dao.get(id);
                if (t != null) {
                    resp.getWriter().write(gson.toJson(t));
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Task t = parseRequest(req, Task.class);
        if (t == null || t.getTitle() == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        dao.add(t);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(t));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Task t = parseRequest(req, Task.class);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            t.setId(id);
            dao.update(t);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(t));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            dao.delete(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private <T> T parseRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }
}