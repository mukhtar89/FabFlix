import java.sql.*;
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
 * Servlet implementation class MovieList
 */
@WebServlet("/MovieList")
public class MovieList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource dataSource;
    private Connection connection;
	headerFooter base = new headerFooter();
	String sort_by = null;
	int ipp = 0;
	
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
		PrintWriter out = response.getWriter();
		sort_by = request.getParameter("by");
		String query=null;
		String query_count="0";
	String spage_id= request.getParameter("page_id");
	String sipp= request.getParameter("ipp");
	if(spage_id == null)
		spage_id ="1";
	String orderby= request.getParameter("order");
	String order_accord="title";
	String ordered_state ="t_asc";
		if(orderby==null)
			orderby ="ASC";
		else{
			ordered_state=orderby;
			switch(orderby){
			case "t_asc":
				orderby ="ASC";
				break;
				case "t_desc":
					orderby ="DESC";
					break;
				case "d_asc":
					orderby ="ASC";
					order_accord="year";
					break;
				case "d_desc":
					orderby ="DESC";
					order_accord="year";
					break;
			}
		}
	if(sipp==null)
		sipp="5";
	int page_id=Integer.parseInt(spage_id)-1;
	ipp=Integer.parseInt(sipp);
	
		switch(sort_by){
		
		case "genre" :
			String genre_id=request.getParameter("arg");
			query="select * from movies "+
			      "join  genres_in_movies on movies.id=genres_in_movies.movie_id "+
				  "where genres_in_movies.genre_id="+genre_id+" order by title "+orderby+" LIMIT "+ipp+" OFFSET "+ipp*page_id; 
			query_count="select count(*) from movies "+
				      "join  genres_in_movies on movies.id=genres_in_movies.movie_id "+
					  "where genres_in_movies.genre_id="+genre_id; 
			
			break;
		case "title" :
			String title=request.getParameter("arg");
			query="Select * from movies where title like '"+title+"%'"+" order by title "+orderby+"  LIMIT "+ipp+" OFFSET "+ipp*page_id ;
			query_count="Select count(*) from movies where title like '"+title+"%'";
			
			break;
			
		case "search" :
			String search_term=request.getParameter("arg");
			query="Select * from movies where title like '%"+search_term.replace("'", "''")+"%'"+" order by title "+orderby+"  LIMIT "+ipp+" OFFSET "+ipp*page_id;
			query_count="Select count(*) from movies where title like '% "+search_term.replace("'", "''")+"%'";
			//out.println(query);
			break;
		default:
			break;
		}
		
		out.println(base.header());
		out.println("<HEAD><TITLE>Movie Search</TITLE></HEAD>");
		out.println(base.banner());

		try {
			connection = (Connection) dataSource.getConnection();
			PreparedStatement ps_movies = (PreparedStatement) connection.prepareStatement(query);
			ResultSet movies = ps_movies.executeQuery();
			print(movies, response, request);
			PreparedStatement ps_movies_count = (PreparedStatement) connection.prepareStatement(query_count);
			ResultSet countrs=ps_movies_count.executeQuery();
			countrs.next();
			Integer count = countrs.getInt(1);
			int i=page_id-2;
			if(i<=0)i=1;
			out.println("<tr style=\"background-color:#00CCFF;\"><td align=\"center\">");
			for(;i<=page_id+5 && i<=count/ipp;i++)
				out.println("<a style=\"float:left\" class=\"ft_links\" href=/Fabflix/MovieList?by="+sort_by+"&arg="+request.getParameter("arg")+"&page_id="+(i)+"&ipp="+ipp+"&order="+orderby+" >"+i+"</a>");
			out.println("<span style=\"float:left;color:white;margin-top:10px;font-weight: bold;\">&nbsp&nbsp&nbsp<--Page Number</span>");
			for(int j=5;j<=25;j=j+5)
				out.println("<a style=\"float:right\" class=\"ft_links\" href=/Fabflix/MovieList?by="+sort_by+"&arg="+request.getParameter("arg")+"&page_id="+(1)+"&ipp="+j+"&order="+orderby+">"+j+"</a>");	
			out.println("<span style=\"float:right;color:white;margin-top:10px;font-weight: bold;\">Items per page-->&nbsp&nbsp&nbsp</span>");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println("</td></tr></table>");
		out.println(base.footer());
	}

	public void print(ResultSet result, HttpServletResponse response, HttpServletRequest request) throws SQLException, IOException
	{
		//connection = (Connection) dataSource.getConnection();
		//String query = "Select * from movies where title like 'I%'";
		//PreparedStatement ps_movies = (PreparedStatement) connection.prepareStatement(query);
		ResultSet movies = result;
		PrintWriter out = response.getWriter();
		int size = 0;

		out.println("<table id=\"srch_res\"><tr style=\"background-color:#00CCFF;\"><th>");
		out.println("<h2 style=\"margin-left:10%;\">Search Results:</h2></th></tr>"
				+ "<tr style=\"background-color:#00CCFF;\"><td align=\"center\" >");
		out.println("<div align\"center\"><a class=\"sort_links\" href=/Fabflix/MovieList?by="+sort_by+"&arg="+request.getParameter("arg")+"&page_id="+(1)+"&ipp="+ipp+"&order=t_asc>"
				+ "<img src=\"http://goo.gl/QklvbJ?gdriveurl\" height='34' width='34'>Title</a>");
		out.println("<a class=\"ft_links\" href=/Fabflix/MovieList?by="+sort_by+"&arg="+request.getParameter("arg")+"&page_id="+(1)+"&ipp="+ipp+"&order=t_desc >Title</a>");
		out.println("<a class=\"ft_links\" href=/Fabflix/MovieList?by="+sort_by+"&arg="+request.getParameter("arg")+"&page_id="+(1)+"&ipp="+ipp+"&order=d_asc >Year-> Asec</a>");
		out.println("<a class=\"ft_links\" href=/Fabflix/MovieList?by="+sort_by+"&arg="+request.getParameter("arg")+"&page_id="+(1)+"&ipp="+ipp+"&order=d_desc >Year-> Dsec</a>"
				+ "</div><br></td></tr>");
		if (movies.next())
		{
			while (movies.next());
				size++;
			movies.first();
	     	do
			{
				String star_query = "Select distinct(a.first_name), a.last_name, a.id from stars a "
						+ "where a.id in (select distinct(b.star_id) from stars_in_movies b "
						+ "where b.movie_id in (select distinct(c.id)  from movies c where c.title = '" + movies.getString("title").replace("'", "''") +"'));";
				PreparedStatement ps_stars = (PreparedStatement) connection.prepareStatement(star_query);
				ResultSet stars = ps_stars.executeQuery();
				
				String genre_query = "Select distinct(a.name) from genres a "
						+ "where a.id in (select distinct(b.genre_id) from genres_in_movies b where b.movie_id in "
						+ "(select distinct(c.id)  from movies c where c.title = '" + movies.getString("title").replace("'", "''") +"'));";
				PreparedStatement ps_genres = (PreparedStatement) connection.prepareStatement(genre_query);
				ResultSet genres = ps_genres.executeQuery();
				
				
				
				out.println("<tr><td><br><br>" + 
						"<div>" + 
						"<table  id=\"movie_search\"><tr><td width=\"20%;\"><div id=\"mov_list\">" + 
						"<img  style=\"position:absolute;z-index:1;margin-top:30px;margin-left:75px;\" src=\"" + movies.getString("banner_url") + "\"  alt=\"" + movies.getString("title") + " DVD Cover\" height='188' width='120'>" + 
						"<img style=\"z-index:2;\" src=\"http://gateway.hopto.org:9000/fabflix/images/short-case.png\" height='250' width='255'></div></td>" + 
						"<td width=\"40%;\"><div id=\"mov_det\">" + 
						"<div style=\"float:left;width:10%;\"><span style=\"font-weight: bold;\">Movie: </span></div>"
						+ "<div style=\"float:right;width:90%;\"><a class=\"ag_links\" style=\"font-size:18px;\" href=\"/Fabflix/Movie?MovieID=" + movies.getString("id") + "\">" + movies.getString("title") + "</a></div><br>" + 
						"<br><span>Year: " + movies.getString("year") + "</span><br>" + 
						"<br><span>Director: " + movies.getString("director") + "</span><br>" + 
						"<br><div style=\"float:left;width:10%;\"><span style=\"font-style: italic;\">Actors: </span></div><div style=\"float:right;width:90%;\"><span>");
				while (stars.next())
				{
					out.println("<a class=\"ag_links\" href=\"/Fabflix/Star?StarID=" + stars.getString("id") + "\">"
							+ stars.getString("first_name") + " " + stars.getString("last_name") + "</a>");

				} 
	         out.println("</span></div><br><br><span style=\"font-style: italic;\">Genre: ");
				String genre_list = "";
				while (genres.next())
				{
					genre_list += (genres.getString("name") + ", ");
				}
				genre_list = genre_list.substring(0, genre_list.length()-2);
				out.println(genre_list + "</span><br><br>");
	         
	         
	         
	         out.println("<span>Price: [$12.45]</span><br></div></td></tr>" + 
						"<tr><td class=\"cart\"><img style=\"float:left;padding-left:30%;\" src=\"http://goo.gl/xuA1xS?gdriveurl\" height=\"24\" width=\"24\">" + 
						"<a class=\"links\" href='/Fabflix/Cart?MovieID=" + movies.getString("id") + "&qty=1&req=Add'\">&nbspAdd to My Cart</a></td>" + 
						"<td class=\"cart\"><img style=\"float:left;padding-left:30%;\" src=\"http://goo.gl/rXhBkP?gdriveurl\" height=\"24\" width=\"24\">" + 
						"<a class=\"links\" href='" + movies.getString("trailer_url") + "'\">&nbspWatch Trailer</a></td>" + 
						"</tr></table>" + 
						"</div>" + 
						"<br></td></tr>"); 
	         if (size > 1)
	        	 out.println("<hr id=\"line\">");
	         size--;
			} while (movies.next());
			

		}
		else
		{
			//String mess="Username or password incorrect";
			//response.sendRedirect("/Fabflix/index.html?message="+mess);
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}