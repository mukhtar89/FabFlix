import java.sql.*;
import java.util.Stack;
import java.io.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class Movie
 */
@WebServlet("/Movie")
public class Movie extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource dataSource;
    private Connection connection;
       
    /**
     * @param connection 
     * @see HttpServlet#HttpServlet()
     */ 
    public void init() throws ServletException
    {
    	try {
            // Get DataSource
            Context initContext  = new InitialContext();
            dataSource = (DataSource) initContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		// TODO Auto-generated method stub;
		try {
			print("Inception", response, request);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void print(String title, HttpServletResponse response, HttpServletRequest request) throws SQLException, IOException
	{
		connection = (Connection) dataSource.getConnection();
		int movie_id = Integer.parseInt(request.getParameter("MovieID"));
		String query = "Select * from movies where id like '" + movie_id + "'";
		PreparedStatement ps_movies = (PreparedStatement) connection.prepareStatement(query);
		ResultSet movies = ps_movies.executeQuery();
		PrintWriter out = response.getWriter();
		
		String message = request.getParameter("message");
		out.println("<HTML><HEAD><TITLE>login</TITLE></HEAD>");
		 out.println("<BODY><H1 ALIGN=\"CENTER\">Movie Details</H1>");
		 out.println("</CENTER></FORM></BODY></HTML>");
		 out.println("<a href=\"javascript:history.go(-1)\">Go back to previous page</a>");
		 out.println("<style>"
				+ "#container {"
				+ "padding:10%"
		 		+ "height:250px;"
				+ "margin:20%;}"
		 		+ "#details {"
		 		+ "text-align:left;"
		 		+ "padding:5px;"
		 		+ "width:60%;"
		 		+ "color:white;"
		 		+ "background-color:black;"
		 		+ "height:220px;"
		 		+ "float:right;"
		 		+ "}"
		 		+ "#image {"
		 		+ "width:35%;"
		 		+ "float:left;"
		 		+ "background-color:black;"
		 		+ "height:220px;"
		 		+ "padding:5px;"
		 		+ "}"
		 		+ "</style>");
		if (movies.next())
		{
			do
			{
				String star_query = "Select distinct(a.first_name), a.last_name, a.id from stars a "
						+ "where a.id in (select distinct(b.star_id) from stars_in_movies b "
						+ "where b.movie_id in (select distinct(c.id)  from movies c where c.title = '" + movies.getString("title") +"'));";
				PreparedStatement ps_stars = (PreparedStatement) connection.prepareStatement(star_query);
				ResultSet stars = ps_stars.executeQuery();
				
				String genre_query = "Select distinct(a.name) from genres a "
						+ "where a.id in (select distinct(b.genre_id) from genres_in_movies b where b.movie_id in "
						+ "(select distinct(c.id)  from movies c where c.title = '" + movies.getString("title") +"'));";
				PreparedStatement ps_genres = (PreparedStatement) connection.prepareStatement(genre_query);
				ResultSet genres = ps_genres.executeQuery();
				
				out.println("<div id=\"container\"><div id=\"image\">");
				out.println("<img style=\"width:110;height;160;\"src=\"" + movies.getString("banner_url")
						+ "\" alt=\"" + movies.getString("title") + " DVD Cover\"></div>");
				out.println("<div id=\"details\"><span class=\"title\">Title = " + movies.getString("title") + "</span><br>");
				out.println("<span class=\"title\">Year = " + movies.getString("year") + "</span><br>");
				out.println("<span class=\"title\">Director = " + movies.getString("director") + "</span><br>");
				
				out.println("<span class=\"title\">Actors = ");
				int size = 0;
				while (stars.next())
					size++;
				stars.first();
				do
				{
					out.println("<a href=\"/FabFlix/Star?StarID=" + stars.getString("id") + "\">"
							+ stars.getString("first_name") + " " + stars.getString("last_name") + "</a>");
					if (size != 1)
						out.println(", ");
					size--;
				} while (stars.next());
				out.println("</span><br>");
				
				out.println("<span class=\"title\">Genre = ");
				String genre_list = "";
				while (genres.next())
				{
					genre_list += (genres.getString("name") + ", ");
				}
				genre_list = genre_list.substring(0, genre_list.length()-2);
				out.println(genre_list + "</span><br>");
				
				out.println("<a href=\"" + movies.getString("trailer_url") + "\">Watch Trailer</a></div></div><br><br><br><br>");
			} while (movies.next());
		}
		else
		{
			String mess="Username or password incorrect";
			response.sendRedirect("/FabFlix/index.html?message="+mess);  
			out.println("<tr>" + "<td>" + message+ "</td>" +"</tr>");
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
