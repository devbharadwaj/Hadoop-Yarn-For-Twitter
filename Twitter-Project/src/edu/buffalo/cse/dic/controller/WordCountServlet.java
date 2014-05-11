package edu.buffalo.cse.dic.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import edu.buffalo.cse.dic.mapreduce.MapReduceJob;
import edu.buffalo.cse.dic.mapreduce.WordCount;
import edu.buffalo.cse.dic.model.JsonGenerator;


/**
 * Servlet implementation class DataAggregator
 */
@WebServlet("/WordCountServlet")
public class WordCountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WordCountServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		JsonGenerator jsonObject = new JsonGenerator("Data/wordcount.txt");
		PrintWriter out = response.getWriter();
		out.println(jsonObject.getJson());	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
