

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class Cart
 */
@WebServlet("/Cart")
public class Cart extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static String User = null;
    private static String Pass = null;
    private static String Page = "/FabFlix/Cart";
	private DataSource dataSource;
    private Connection connection;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Cart() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException 
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
	 * @see Servlet#getServletConfig()
	 */
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
	    synchronized(session) 
	    {
	         User = (String) session.getAttribute("User");
	         Pass = (String) session.getAttribute("Pass");
	         session.setAttribute("Page", Page);
        }
        if (User.isEmpty() || Pass.isEmpty())
       	 response.sendRedirect("/FabFlix/index.html");
	    try {
			print(response, request);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void print(HttpServletResponse response, HttpServletRequest request) throws SQLException, IOException
	{
		connection = (Connection) dataSource.getConnection();
		String custid = "Select id from customers where first_name = '" + User + "' and password = '" + Pass + "';";
		PreparedStatement ps_custid = (PreparedStatement) connection.prepareStatement(custid);
		ResultSet id = ps_custid.executeQuery();
		id.next();
		int customer_id = Integer.parseInt(id.getString("id"));
		
		int movie_id = Integer.parseInt(request.getParameter("MovieID"));
		String movie_check = "Select * from cart where title in "
				+ "(Select distinct(title) from movies where id = '" + movie_id + "')"
						+ " and customer_id = '" + customer_id + "';";
		PreparedStatement ps_cart_check = (PreparedStatement) connection.prepareStatement(movie_check);
		ResultSet cart_check = ps_cart_check.executeQuery();
		
		if (cart_check.next())
		{
			String update = "update cart set quantity = " + Integer.parseInt(cart_check.getString("quantity"))+1 + " where title in "
					+ "(Select distinct(title) from movies where id = '" + movie_id + "')"
							+ " and customer_id = '" + customer_id + "';";
			PreparedStatement ps_cart_update = (PreparedStatement) connection.prepareStatement(update);
			ps_cart_update.executeUpdate();
		}
		else
		{
			String movie = "Select * from movies where id = '" + movie_id + "';";
			PreparedStatement ps_movie = (PreparedStatement) connection.prepareStatement(movie);
			ResultSet movies = ps_movie.executeQuery();
			movies.next();
			String insert = "INSERT INTO cart ('title', 'price', 'quantity', 'customer_id', 'movie_id') "
					+ "VALUES ('" + movies.getString("title") +  "(" + movies.getString("year") + ")', '12.35', '1', '" + customer_id + "', '" + movie_id + "');";
			PreparedStatement ps_cart_insert = (PreparedStatement) connection.prepareStatement(insert);
			ps_cart_insert.executeUpdate();
		}
		
		String query = "Select * from cart where customer_id like '" + customer_id + "'";
		PreparedStatement ps_cart = (PreparedStatement) connection.prepareStatement(query);
		ResultSet cart = ps_cart.executeQuery();
		
		PrintWriter out = response.getWriter();
		
		out.println("<HTML><HEAD><TITLE>login</TITLE></HEAD>");
		out.println("<BODY><H1 ALIGN=\"CENTER\">Shopping Cart</H1></CENTER>");
		out.println("<table border>"
				+ "<tr><th>Movie Title</th>"
				+ "<th>Price</th>"
				+ "<th>Quantity</th>"
				+ "<th>Update</th>"
				+ "<th>Remove</th></tr>");
		
		while(cart.next())
		{
			out.println("<tr><td>" + cart.getString("title") + "</td>");
			out.println("<td>" + cart.getString("price") + "</td>");
			out.println("<td>" + cart.getString("quantity") + "</td>");
			out.println("<td>" + cart.getString("quantity") + "</td>");
			out.println("<td>" + cart.getString("quantity") + "</td></tr>");
		}
		
		 
		out.println("</table>");
		out.println("</BODY></HTML>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
