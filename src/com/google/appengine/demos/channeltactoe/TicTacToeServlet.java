package com.google.appengine.demos.channeltactoe;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class TicTacToeServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    String thisURL = req.getRequestURI();
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    Game game = null;
    String gameKey = req.getParameter("g");
    String userId = userService.getCurrentUser().getUserId();
    if (gameKey != null) {
      game = pm.getObjectById(Game.class, KeyFactory.stringToKey(gameKey));
      if (game.getUserY() == null) {
        game.setUserY(userId);
      }
    } else {
      game = new Game(userId, null, "         ", true);
      pm.makePersistent(game);
      gameKey = KeyFactory.keyToString(game.getKey());
    }
    pm.close();
    
    if (req.getUserPrincipal() != null) {
      FileReader reader = new FileReader("index.html");
      CharBuffer buffer = CharBuffer.allocate(16384);
      reader.read(buffer);
      String index = new String(buffer.array());
      index = index.replaceAll("\\{\\{ game_key \\}\\}", gameKey);
      index = index.replaceAll("\\{\\{ me \\}\\}", userId);
      
      resp.setContentType("text/html");
      resp.getWriter().write(index);
    } else {
      resp.getWriter().println("<p>Please <a href=\"" +
          userService.createLoginURL(thisURL) + "\">sign in</a>.</p>");
    }
  }
}
