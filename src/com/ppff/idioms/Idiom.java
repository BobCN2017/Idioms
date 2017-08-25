package com.ppff.idioms;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Idiom")
public class Idiom extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void init() throws ServletException {
		InputStream idioms=getServletContext().getResourceAsStream("/raw/idiomblowthirtythousand.txt");
		InputStream fourwords=getServletContext().getResourceAsStream("/raw/allfourword.txt");
		IdiomUtil.init(idioms,fourwords);

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String text = new String(request.getParameter("q").getBytes(
				"iso-8859-1"), "utf-8");
		String userId = new String(request.getParameter("id").getBytes(
				"iso-8859-1"), "utf-8");
	
//		String text = request.getParameter("q");
//		String userId = request.getParameter("id");
		if (text.trim().equalsIgnoreCase("quit")) {
			IdiomUtil.removeUsersDate(Long.valueOf(userId));
			return;
		}

		
		String result = IdiomUtil.concatenateDragon(text, Long.valueOf(userId));
//		System.out.println(text + "userId:" + userId+" result:"+result);
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().write(result);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
