package 영지풀이;
//빌드사이트 추가중 
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class Main {
	public static void main(String[] args) {
		App app = new App();
		app.start();
	}
}

// Session
// 현재 사용자가 이용중인 정보
// 이 안의 정보는 사용자가 프로그램을 사용할 때 동안은 계속 유지된다.
class Session {
	private Member loginedMember;
	private Board currentBoard;

	public Member getLoginedMember() {
		return loginedMember;
	}

	public void setLoginedMember(Member loginedMember) {
		this.loginedMember = loginedMember;
	}

	public Board getCurrentBoard() {
		return currentBoard;
	}

	public void setCurrentBoard(Board currentBoard) {
		this.currentBoard = currentBoard;
	}

	public boolean isLogined() {
		return loginedMember != null;
	}
}

// Factory
// 프로그램 전체에서 공유되는 객체 리모콘을 보관하는 클래스

class Factory {
	private static Session session;
	private static DB db;
	private static BuildService buildService;
	private static ArticleService articleService;
	private static ArticleDao articleDao;
	private static MemberService memberService;
	private static MemberDao memberDao;
	private static Scanner scanner;

	public static Session getSession() {
		if (session == null) {
			session = new Session();
		}
		return session;
	}

	public static Scanner getScanner() {
		if (scanner == null) {
			scanner = new Scanner(System.in);
		}

		return scanner;
	}

	public static DB getDB() {
		if (db == null) {
			db = new DB();
		}

		return db;
	}

	public static ArticleService getArticleService() {
		if (articleService == null) {
			articleService = new ArticleService();
		}
		return articleService;
	}

	public static ArticleDao getArticleDao() {
		if (articleDao == null) {
			articleDao = new ArticleDao();
		}
		return articleDao;
	}

	public static MemberService getMemberService() {
		if (memberService == null) {
			memberService = new MemberService();
		}
		return memberService;
	}

	public static MemberDao getMemberDao() {
		if (memberDao == null) {
			memberDao = new MemberDao();
		}
		return memberDao;
	}

	public static BuildService getBuildService() {
		if (buildService == null) {
			buildService = new BuildService();
		}

		return buildService;
	}
}

// App
class App {
	private Map<String, Controller> controllers;

	// 컨트롤러 만들고 한곳에 정리
	// 나중에 컨트롤러 이름으로 쉽게 찾아쓸 수 있게 하려고 Map 사용
	void initControllers() {
		controllers = new HashMap<>();
		controllers.put("build", new BuildController());
		controllers.put("article", new ArticleController());
		controllers.put("member", new MemberController());
	}

	public App() {
		// 컨트롤러 등록
		initControllers();

		// 관리자 회원 생성
		Factory.getMemberService().join("admin", "admin", "관리자");
		// 공지사항 게시판 생성
		Factory.getArticleService().makeBoard("공지사항", "notice");

		// 현재 게시판을 1번 게시판으로 선택
		Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(1));
		// 임시 : 현재 로그인 된 회원은 1번 회원으로 지정, 이건 나중에 회원가입, 로그인 추가되면 제거해야함
//		Factory.getSession().setLoginedMember(Factory.getMemberService().getMember(1));
	}

	public void start() {

		while (true) {
			System.out.printf("명령어 : ");
			String command = Factory.getScanner().nextLine().trim();

			if (command.length() == 0) {
				continue;
			} else if (command.equals("exit")) {
				break;
			}

			Request reqeust = new Request(command);

			if (reqeust.isValidRequest() == false) {
				continue;
			}

			if (controllers.containsKey(reqeust.getControllerName()) == false) {
				continue;
			}

			controllers.get(reqeust.getControllerName()).doAction(reqeust);
		}

		Factory.getScanner().close();
	}
}

// Request
class Request {
	private String requestStr;
	private String controllerName;
	private String actionName;
	private String arg1;
	private String arg2;
	private String arg3;

	boolean isValidRequest() {
		return actionName != null;
	}

	Request(String requestStr) {
		this.requestStr = requestStr;
		String[] requestStrBits = requestStr.split(" ");
		this.controllerName = requestStrBits[0];

		if (requestStrBits.length > 1) {
			this.actionName = requestStrBits[1];
		}

		if (requestStrBits.length > 2) {
			this.arg1 = requestStrBits[2];
		}

		if (requestStrBits.length > 3) {
			this.arg2 = requestStrBits[3];
		}

		if (requestStrBits.length > 4) {
			this.arg3 = requestStrBits[4];
		}
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}

	public String getArg3() {
		return arg3;
	}

	public void setArg3(String arg3) {
		this.arg3 = arg3;
	}
}

// Controller
abstract class Controller {
	abstract void doAction(Request reqeust);
}

class ArticleController extends Controller {
	private ArticleService articleService;

	ArticleController() {
		articleService = Factory.getArticleService();
	}

	public void setArticleService(ArticleService articleService) {
		this.articleService = articleService;
	}

	public void doAction(Request reqeust) {
		// 나중에 로그인 유무 확인하는 코드 작성하기.
		if (reqeust.getActionName().equals("list")) {
			actionList(reqeust);
		} else if (reqeust.getActionName().equals("write")) {
			actionWrite(reqeust);
		} else if (reqeust.getActionName().equals("modify")) {
			actionModify(reqeust);
		} else if (reqeust.getActionName().equals("delete")) {
			actionDelete(reqeust);
		} else if (reqeust.getActionName().equals("detail")) {
			actionDetail(reqeust);
		} else if (reqeust.getActionName().equals("listBoard")) {
			actionListBoard(reqeust);
		} else if (reqeust.getActionName().equals("changeBoard")) {
			actionChangeBoard(reqeust);
		} else {
			if(Factory.getSession().getLoginedMember()!=null) {
				if (Factory.getSession().getLoginedMember().getId() == 1) {
					if (reqeust.getActionName().equals("createBoard")) {
						actionCreateBoard(reqeust);
					} else if (reqeust.getActionName().equals("deleteBoard")) {
						actionDeleteBoard(reqeust);
					}
				} else {
					System.out.println("관리자 권한이 필요합니다.");
				}
			}
			else {
				System.out.println("로그인 후 이용 가능합니다.");
			}
		}
	}

	private void actionChangeBoard(Request reqeust) {
		String code = reqeust.getArg1();
		if (code != null) {
			// 해당 코드의 게시판이 존재하는지.
			// 존재한다면 게시판 변경.
			int boardId = articleService.isExistBoard(code);
			if (boardId > 0) {
				articleService.changeBoard(boardId);
			} else {
				System.out.println("게시판 변경 실패 사유 : 해당 코드 게시판 부재");
			}
		} else {
			System.out.println("게시판 변경 실패 사유 : 코드 미입력");
		}
	}

	private void actionListBoard(Request reqeust) {
		articleService.listBoard();
	}

	private void actionDeleteBoard(Request reqeust) {
		String deleteCode = reqeust.getArg1();
		if (deleteCode == null) {
			System.out.println("게시판 삭제 실패 사유 : 삭제할 게시판의 코드 미입력");
		} else {
			// 해당 코드의 게시판이 있는지
			int boardId = articleService.isExistBoard(deleteCode);
			if (boardId > 0) {
				// 있다면 삭제하는 코드(삭제하는 게시판의 게시물들도 다 삭제)
				articleService.deleteBoard(boardId);
			} else {
				System.out.println("해당 코드의 게시판은 존재하지 않습니다.");
			}
		}
	}

	private void actionCreateBoard(Request reqeust) {
		System.out.println("생성할 게시판과 코드를 입력하세요.");
		System.out.print("게시판 이름 : ");
		String boardName = Factory.getScanner().nextLine().trim();
		System.out.print("코드 : ");
		String boardCode = Factory.getScanner().nextLine().trim();
		// 동일한 이름 혹은 코드가 있는 게시판이 존재하지 않을 경우 실행하도록.
		if (!articleService.findBoard(boardName, boardCode)) {
			Factory.getArticleService().makeBoard(boardName, boardCode);
		} else {
			System.out.println("게시판 생성 실패 사유 : 동일한 이름/코드의 게시판이 존재");
		}
	}

	private void actionDetail(Request reqeust) {
//		if (Factory.getSession().getLoginedMember() != null) {
		int detailArticleNum = Integer.parseInt(reqeust.getArg1());
		articleService.detailArticle(detailArticleNum);
//		} else {
//			System.out.println("게시물 상세보기 / 비회원 접근 불가");
//		}
	}

	private void actionDelete(Request reqeust) {
		if (Factory.getSession().getLoginedMember() != null) {
			int deleteArticleNum = -1;
			try {
				deleteArticleNum = Integer.parseInt(reqeust.getArg1());
			} catch (Exception e) {
			}
			if (deleteArticleNum != -1) {
				articleService.deleteArticle(deleteArticleNum);
			} else {
				System.out.println("게시물 삭제 실패 사유 : 번호 미입력");
			}
		} else {
			System.out.println("게시글 삭제 / 비회원 접근 불가");
		}
	}

	private void actionModify(Request reqeust) {
		if (Factory.getSession().getLoginedMember() != null) {
			int number = -1;
			try {
				number = Integer.parseInt(reqeust.getArg1());
			} catch (Exception e) {
			}
			int result = articleService.isExistArticle(number);
			if (result > 0) {
				System.out.printf("제목 : ");
				String title = Factory.getScanner().nextLine();
				System.out.printf("내용 : ");
				String body = Factory.getScanner().nextLine();
				articleService.modifyArticle(number, title, body);
				System.out.println(number + "번 글이 수정되었습니다.");
			} else if (result == -1) {
				System.out.println("해당 번호의 게시글이 존재하지 않습니다.");
			} else if (result == 0) {
				System.out.println("게시글 수정은 작성자 본인만 가능합니다.");
			}
		} else {
			System.out.println("게시글 수정 / 비회원 접근 불가");
		}
	}

	private void actionList(Request reqeust) {
		int pageNum = 1;
		// page 숫자를 입력하지 않았을 경우, 1p를 보여줌.
		try {
			pageNum = Integer.parseInt(reqeust.getArg1());
		} catch (Exception e) {
		}
		String listSearchKeyword = "";
		try {
			if (reqeust.getArg2().trim().length() > 0) {
				listSearchKeyword = reqeust.getArg2();
			}
		} catch (Exception e) {
		}

		articleService.listArticlePage(pageNum, listSearchKeyword);
	}

	private void actionWrite(Request reqeust) {
		if (Factory.getSession().getLoginedMember() != null) {
			System.out.printf("제목 : ");
			String title = Factory.getScanner().nextLine();
			System.out.printf("내용 : ");
			String body = Factory.getScanner().nextLine();

			// 현재 게시판 id 가져오기
			int boardId = Factory.getSession().getCurrentBoard().getId();

			// 현재 로그인한 회원의 id 가져오기
			int memberId = Factory.getSession().getLoginedMember().getId();
			int newId = articleService.write(boardId, memberId, title, body);

			System.out.printf("%d번 글이 생성되었습니다.\n", newId);
		} else {
			System.out.println("게시글 생성 / 비회원 접근 불가");
		}
	}
}

class MemberController extends Controller {
	private MemberService memberService;

	MemberController() {
		memberService = Factory.getMemberService();
	}

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}

	public void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("logout")) {
			actionLogout(reqeust);
		} else if (reqeust.getActionName().equals("whoami")) {
			actionWhoAmI(reqeust);
		} else if (reqeust.getActionName().equals("login")) {
			actionLogin(reqeust);
		} else if (reqeust.getActionName().equals("join")) {
			actionJoin(reqeust);
		}
	}

	private void actionJoin(Request reqeust) {
		System.out.print("ID: ");
		String loginId = Factory.getScanner().nextLine().trim();
		System.out.print("PW: ");
		String loginPw = Factory.getScanner().nextLine().trim();
		System.out.print("닉네임: ");
		String name = Factory.getScanner().nextLine().trim();
		int m = memberService.join(loginId, loginPw, name);
		if (m == -1) {
			System.out.println("회원가입 실패 사유 : ID 중복");
		}
	}

	private void actionLogin(Request reqeust) {
		if (Factory.getSession().getLoginedMember() != null) {
			System.out.println("현재 로그인 상태입니다.");
		} else {
			System.out.print("ID: ");
			String loginId = Factory.getScanner().nextLine().trim();
			System.out.print("PW: ");
			String loginPw = Factory.getScanner().nextLine().trim();
			Member m = memberService.findMember(loginId, loginPw);
			if (m != null) {
				Factory.getSession().setLoginedMember(m);
				System.out.println(m.getName() + "님, 안녕하세요.");
			} else {
				System.out.println("아이디 혹은 비밀번호를 다시 확인하세요.");
			}
		}
	}

	private void actionWhoAmI(Request reqeust) {
		Member m = Factory.getSession().getLoginedMember();
		if (m == null) {
			System.out.println("비회원");
		} else {
			System.out.println(m.getId() + "번째 가입자, " + m.getName() + " 님");
		}
	}

	private void actionLogout(Request reqeust) {
		Member m = Factory.getSession().getLoginedMember();
		if (m == null) {
			System.out.println("이미 로그아웃 상태입니다.");
		} else {
			System.out.println(m.getName() + "님, 안녕히 가세요.");
			Factory.getSession().setLoginedMember(null);
		}
	}
}

class BuildController extends Controller {
	private BuildService buildService;

	BuildController() {
		buildService = Factory.getBuildService();
	}

	@Override
	void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("site")) {
			actionSite(reqeust);
		}
	}

	private void actionSite(Request reqeust) {
		buildService.buildSite();
	}
}

// Service
class BuildService {
	ArticleService articleService;

	BuildService() {
		articleService = Factory.getArticleService();
	}

	public void buildSite() {
		Util.makeDir("site");
		Util.makeDir("site/home");
		Util.makeDir("site/article");

		String head = Util.getFileContents("site_template/part/head.html");
		String foot = Util.getFileContents("site_template/part/foot.html");

		// 각 게시판 별 게시물리스트 페이지 생성
		List<Board> boards = articleService.getBoards();

		for (Board board : boards) {
			String fileName = board.getCode() + "-list-1.html";

			String html = "";

			List<Article> articles = articleService.getArticlesByBoardCode(board.getCode());

			String template = Util.getFileContents("site_template/article/list.html");

			for (Article article : articles) {
				html += "<tr>";
				html += "<td>" + article.getId() + "</td>";
				html += "<td>" + article.getRegDate() + "</td>";
				html += "<td><a href=\"" + article.getId() + ".html\">" + article.getTitle() + "</a></td>";
				html += "</tr>";
			}

			html = template.replace("${TR}", html);

			html = head + html + foot;

			Util.writeFileContents("site/article/" + fileName, html);
		}

		// 게시물 별 파일 생성
		List<Article> articles = articleService.getArticles();

		for (Article article : articles) {
			String html = "";

			html += "<div>제목 : " + article.getTitle() + "</div>";
			html += "<div>내용 : " + article.getBody() + "</div>";
			if(!articles.get(0).equals(article)) {
				html += "<div><a href=\"" + (article.getId() - 1) + ".html\">이전글</a></div>";
			}
			if(!articles.get(articles.size()-1).equals(article)) {
				html += "<div><a href=\"" + (article.getId() + 1) + ".html\">다음글</a></div>";
			}

			html = head + html + foot;

			Util.writeFileContents("site/article/" + article.getId() + ".html", html);
		}
	}

}

class ArticleService {
	private ArticleDao articleDao;

	ArticleService() {
		articleDao = Factory.getArticleDao();
	}

	public List<Article> getArticlesByBoardCode(String code) {
		return articleDao.getArticlesByBoardCode(code);
	}

	public List<Board> getBoards() {
		return articleDao.getBoards();
	}

	public void changeBoard(int boardId) {
		articleDao.changeBoard(boardId);
	}

	public void listBoard() {
		articleDao.listBoard();
	}

	public void deleteBoard(int boardId) {
		articleDao.deleteBoard(boardId);
	}

	public int isExistBoard(String deleteCode) {
		return articleDao.isExistBoard(deleteCode);
	}

	public boolean findBoard(String boardName, String boardCode) {
		return articleDao.findBoard(boardName, boardCode);
	}

	public void listArticlePage(int pageNum, String keyword) {
		articleDao.listArticlePage(pageNum, keyword);
	}

	public void detailArticle(int num) {
		articleDao.detailArticle(num);
	}

	public void deleteArticle(int num) {
		articleDao.deleteArticle(num);
	}

	public void modifyArticle(int number, String title, String body) {
		articleDao.modifyArticle(number, title, body);
	}

	public int isExistArticle(int number) {
		return articleDao.isExistArticle(number);
	}

//	public void printArticleListByBoard(List<Article> articles) {
//		// 현재 보드에 있는 게시물들을 서치
//		List<Article> articleForPrint = articleDao.findArticleListByBoard(articles);
//		// 찾은 게시물들을 출력
//		articleDao.printArticles(articleForPrint);
//	}

	public int makeBoard(String name, String code) {
		Board oldBoard = articleDao.getBoardByCode(code);

		if (oldBoard != null) {
			return -1;
		}

		Board board = new Board(name, code);
		return articleDao.saveBoard(board);
	}

	public Board getBoard(int id) {
		return articleDao.getBoard(id);
	}

	public int write(int boardId, int memberId, String title, String body) {
		Article article = new Article(boardId, memberId, title, body);
		return articleDao.save(article);
	}

	public List<Article> getArticles() {
		return articleDao.getArticles();
	}

}

class MemberService {
	private MemberDao memberDao;

	MemberService() {
		memberDao = Factory.getMemberDao();
	}

	public Member findMember(String loginId, String loginPw) {
		return memberDao.findMember(loginId, loginPw);
	}

	public int join(String loginId, String loginPw, String name) {
		Member oldMember = memberDao.getMemberByLoginId(loginId);

		if (oldMember != null) {
			return -1;
		}

		Member member = new Member(loginId, loginPw, name);
		return memberDao.save(member);
	}

	public Member getMember(int id) {
		return memberDao.getMember(id);
	}
}

// Dao
class ArticleDao {
	DB db;

	ArticleDao() {
		db = Factory.getDB();
	}

	public List<Article> getArticlesByBoardCode(String code) {
		return db.getArticlesByBoardCode(code);
	}

	public List<Board> getBoards() {
		return db.getBoards();
	}

	public void changeBoard(int boardId) {
		db.changeBoard(boardId);
	}

	public void listBoard() {
		db.listBoard();
	}

	public void deleteBoard(int boardId) {
		db.deleteBoard(boardId);
	}

	public int isExistBoard(String deleteCode) {
		return db.isExistBoard(deleteCode);
	}

	public boolean findBoard(String boardName, String boardCode) {
		return db.findBoard(boardName, boardCode);
	}

	public void listArticlePage(int num, String keyword) {
		db.listArticlePage(num, keyword);
	}

	public void detailArticle(int num) {
		db.detailArticle(num);
	}

	public void deleteArticle(int num) {
		db.deleteArticle(num);
	}

	public void modifyArticle(int number, String title, String body) {
		db.modifyArticle(number, title, body);
	}

	public int isExistArticle(int number) {
		return db.isExistArticle(number);
	}

//	public void printArticles(List<Article> articleForPrint) {
//		db.printArticlesByBoard(articleForPrint);
//	}
//	public List<Article> findArticleListByBoard(List<Article> articles) {
//		return db.getArticlesListByBoard(articles);
//	}

	public Board getBoardByCode(String code) {
		return db.getBoardByCode(code);
	}

	public int saveBoard(Board board) {
		return db.saveBoard(board);
	}

	public int save(Article article) {
		return db.saveArticle(article);
	}

	public Board getBoard(int id) {
		return db.getBoard(id);
	}

	public List<Article> getArticles() {
		return db.getArticles();
	}

}

class MemberDao {
	DB db;

	MemberDao() {
		db = Factory.getDB();
	}

	public Member findMember(String loginId, String loginPw) {
		return db.findMember(loginId, loginPw);
	}

	public Member getMemberByLoginId(String loginId) {
		return db.getMemberByLoginId(loginId);
	}

	public Member getMember(int id) {
		return db.getMember(id);
	}

	public int save(Member member) {
		return db.saveMember(member);
	}
}

// DB
class DB {
	private Map<String, Table> tables;

	public DB() {
		String dbDirPath = getDirPath();
		Util.makeDir(dbDirPath);

		tables = new HashMap<>();

		tables.put("article", new Table<Article>(Article.class, dbDirPath));
		tables.put("board", new Table<Board>(Board.class, dbDirPath));
		tables.put("member", new Table<Member>(Member.class, dbDirPath));
	}

	public List<Article> getArticlesByBoardCode(String code) {
		List<Article> articles = new ArrayList<>();
		for (Article a : getArticles()) {
			if (getBoardByCode(code).getId() == a.getBoardId()) {
				articles.add(a);
			}
		}
		return articles;
	}

	public void changeBoard(int boardId) {
		if (boardId != Factory.getSession().getCurrentBoard().getId()) {
			Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(boardId));
			System.out.println(getBoard(boardId).getName() + " 게시판으로 이동합니다.");
		} else {
			System.out.println("게시판 변경 실패 사유 : 현재 위치 게시판");
		}
	}

	public void listBoard() {
		System.out.println("게시판 리스트 출력");
		System.out.println("번호 / 게시판 이름 / 게시판 코드 / 게시판 생성 날짜");
		for (Board b : getBoards()) {
			System.out.printf("%d / %s / %s / %s\n", b.getId(), b.getName(), b.getCode(), b.getRegDate());
		}
	}

	public void deleteBoard(int boardId) {
		for (Article a : getArticles()) {
			if (a.getBoardId() == boardId) {
				File file = new File(getDirPath() + "/article/" + a.getId() + ".json");
				if (file.exists()) {
					file.delete();
					System.out.println(a.getId() + ".json 파일을 삭제하였습니다.");
				}
			}
		}
		File file = new File(getDirPath() + "/board/" + boardId + ".json");
		if (file.exists()) {
			String boardName = getBoard(boardId).getName();
			file.delete();
			System.out.println(boardName + " 게시판을 삭제하였습니다.");
			System.out.println(boardId + ".json 파일을 삭제하였습니다.");
		}
	}
//	public void deleteArticle(int num) {
//		File file = new File(getDirPath() + "/article/" + num + ".json");
//		if (file.exists()) {
//			Article article = getArticleById(num);
//			if (article.getMemberId() == Factory.getSession().getLoginedMember().getId()) {
//				file.delete();
//				System.out.println(num + "번 게시글을 삭제했습니다.");
//			} else {
//				System.out.println("게시글 삭제는 작성자 본인만 가능합니다.");
//			}
//		} else {
//			System.out.println("파일이 존재하지 않습니다.");
//		}
//	}

	public int isExistBoard(String deleteCode) {
		for (Board b : getBoards()) {
			if (b.getCode().equals(deleteCode)) {
				return b.getId();
			}
		}
		return 0;
	}

	public boolean findBoard(String boardName, String boardCode) {
		for (Board b : getBoards()) {
			if (b.getCode().equals(boardCode) || b.getName().equals(boardName)) {
				return true;
			}
		}
		return false;
	}

	public void listArticlePage(int pageNum, String keyword) {
		// 서치키워드를 포함하는 글들을 찾도록.
		List<Article> articles = new ArrayList<>();
		for (Article a : getArticles()) {
			if (a.getBoardId() == Factory.getSession().getCurrentBoard().getId()) {
				if (a.getTitle().contains(keyword) || a.getBody().contains(keyword)) {
					articles.add(a);
				}
			}
		}
		// num을 기준으로 출력
		if (articles.size() != 0) {
			System.out.println(articles.size());
			int page = (articles.size() - 1) / 10 + 1;
			if (page < pageNum) {
				System.out.println("존재하지 않는 페이지입니다.\n검색 결과 마지막 페이지는 " + page + " 페이지입니다.");
			} else {
				System.out.println("번호 | 제목 | 작성 날짜 | 작성자");
				for (int i = articles.size() - 1 - (pageNum - 1) * 10; i >= articles.size()-10-(pageNum - 1) * 10; i--) {
					if (i >= 0) {
						Article a = articles.get(i);
						System.out.printf("%d | %s | %s | %s\n", a.getId(), a.getTitle(), a.getRegDate(),
								getMember(a.getMemberId()).getName());
					} else {
						break;
					}
				}
				System.out.printf("[%d / %d]\n", pageNum, articles.size() / 10 + 1);
			}
//			for(int i=(num-1)*10; i<num*10; i++) {
//				if(i>=articles.size()) {
//					break;
//				}
//				Article a = articles.get(i);
//				System.out.printf("%d | %s | %s | %s\n", a.getId(), a.getTitle(), a.getRegDate(),
//						getMember(a.getMemberId()).getName());
//			}
		} else {
			System.out.println("게시물이 존재하지 않습니다.");
		}
	}
//	public List<Article> getArticlesListByBoard(List<Article> articles) {
//	List<Article> articlesByBoard = new ArrayList<>();
//	for (Article article : getArticles()) {
//		if (article.getBoardId() == Factory.getSession().getCurrentBoard().getId()) {
//			// 여기서 문제 발생 => articlesByBoard에 null만 지정한 게 문제. 객체 생성했더니 해결됨.
//			articlesByBoard.add(article);
//		}
//	}
//	return articlesByBoard;
//}
//	public void printArticlesByBoard(List<Article> articleForPrint) {
//		if (articleForPrint != null) {
//			System.out.println("===" + Factory.getSession().getCurrentBoard().getName() + " 리스팅 ===");
//			System.out.println("번호 | 제목 | 작성 날짜 | 작성자");
//			for (Article article : articleForPrint) {
//				System.out.printf("%d | %s | %s | %s\n", article.getId(), article.getTitle(), article.getRegDate(),
//						getMember(article.getMemberId()).getName());
//			}
//		}
//	}

	public void detailArticle(int num) {
		Article a = getArticleById(num);
		if (a != null) {
			System.out.println(a.getId() + "번 게시물 상세보기");
			System.out.println("제목 : " + a.getTitle());
			System.out.println("내용 : " + a.getBody());
			System.out.println("작성자 : " + getMember(a.getMemberId()).getName());
			System.out.println("마지막 수정 날짜 : " + a.getRegDate());
		} else {
			System.out.println("존재하지 않는 게시물입니다.");
		}
	}

	public void deleteArticle(int num) {
		File file = new File(getDirPath() + "/article/" + num + ".json");
		if (file.exists()) {
			Article article = getArticleById(num);
			if (article.getMemberId() == Factory.getSession().getLoginedMember().getId()) {
				file.delete();
				System.out.println(num + "번 게시글을 삭제했습니다.");
			} else {
				System.out.println("게시글 삭제는 작성자 본인만 가능합니다.");
			}
		} else {
			System.out.println("파일이 존재하지 않습니다.");
		}
	}

	public Article getArticleById(int num) {
		List<Article> articles = getArticles();
		for (Article a : articles) {
			if (a.getId() == num) {
				return a;
			}
		}
		return null;
	}

	public void modifyArticle(int number, String title, String body) {
		Article oldArticle = getArticleById(number);
		Article article = new Article(oldArticle.getBoardId(), oldArticle.getMemberId(), title, body);
		article.setId(number);
		// regDate는 수정되는 시간으로 변경

		tables.get("article").saveRow(article);
	}

	public int isExistArticle(int number) {
//		방법1. 예외처리 사용
//		try {
//			boolean a = getArticles().get(number) != null;
//		} catch (Exception e) {
//			return false;
//		}
//		// 파일이 존재하지 않을 경우, 없는 것을 찾으라고 하는 것이기에 에러 발생 -> try-catch로 예외처리
//		return true;

//		방법2.
		List<Article> articles = getArticles();
		for (Article a : articles) {
			if (a.getId() == number) {
				if (a.getMemberId() == Factory.getSession().getLoginedMember().getId()) {
					return number;
				}
				return 0;
			}
		}
		return -1;
	}

	public Member findMember(String loginId, String loginPw) {
		Member m = getMemberByLoginId(loginId);
		if (m == null) {
			m = null;
		} else {
			if (!m.getLoginPw().equals(loginPw)) {
				m = null;
			}
		}
		return m;
	}

	public Member getMemberByLoginId(String loginId) {
		List<Member> members = getMembers();

		for (Member member : members) {
			if (member.getLoginId().equals(loginId)) {
				return member;
			}
		}

		return null;
	}

	public List<Member> getMembers() {
		return tables.get("member").getRows();
	}

	public Board getBoardByCode(String code) {
		List<Board> boards = getBoards();

		for (Board board : boards) {
			if (board.getCode().equals(code)) {
				return board;
			}
		}
		return null;
	}

	public List<Board> getBoards() {
		return tables.get("board").getRows();
	}

	public Member getMember(int id) {
		return (Member) tables.get("member").getRow(id);
	}

	public int saveBoard(Board board) {
		return tables.get("board").saveRow(board);
	}

	public String getDirPath() {
		return "db";
	}

	public int saveMember(Member member) {
		return tables.get("member").saveRow(member);
	}

	public Board getBoard(int id) {
		return (Board) tables.get("board").getRow(id);
	}

	public List<Article> getArticles() {
		return tables.get("article").getRows();
	}

	public int saveArticle(Article article) {
		return tables.get("article").saveRow(article);
	}

	public void backup() {
		for (String tableName : tables.keySet()) {
			Table table = tables.get(tableName);
			table.backup();
		}
	}
}

// Table
class Table<T> {
	private Class<T> dataCls;
	private String tableName;
	private String tableDirPath;

	public Table(Class<T> dataCls, String dbDirPath) {
		this.dataCls = dataCls;
		this.tableName = Util.lcfirst(dataCls.getCanonicalName()); // 소문자화
		this.tableDirPath = dbDirPath + "/" + this.tableName;

		Util.makeDir(tableDirPath);// 폴더생성
	}

	private String getTableName() {
		return tableName;
	}

	public int saveRow(T data) {
		Dto dto = (Dto) data;

		if (dto.getId() == 0) {
			int lastId = getLastId();
			int newId = lastId + 1;
			((Dto) data).setId(newId);
			setLastId(newId);
		}

		String rowFilePath = getRowFilePath(dto.getId());

		Util.writeJsonFile(rowFilePath, data);

		return dto.getId();
	};

	private String getRowFilePath(int id) {
		return tableDirPath + "/" + id + ".json";
	}

	private void setLastId(int lastId) {
		String filePath = getLastIdFilePath();
		Util.writeFileContents(filePath, lastId);
	}

	private int getLastId() {
		String filePath = getLastIdFilePath();

		if (Util.isFileExists(filePath) == false) {
			int lastId = 0;
			Util.writeFileContents(filePath, lastId);
			return lastId;
		}

		return Integer.parseInt(Util.getFileContents(filePath));
	}

	private String getLastIdFilePath() {
		return this.tableDirPath + "/lastId.txt";
	}

	public T getRow(int id) {
		return (T) Util.getObjectFromJson(getRowFilePath(id), dataCls);
	}

	public void backup() {

	}

	void delete(int id) {
		/* 구현 */
	};

	List<T> getRows() {
		int lastId = getLastId();

		List<T> rows = new ArrayList<>();

		for (int id = 1; id <= lastId; id++) {
			T row = getRow(id);

			if (row != null) {
				rows.add(row);
			}
		}

		return rows;
	};
}

// DTO
abstract class Dto {
	private int id;
	private String regDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	Dto() {
		this(0);
	}

	Dto(int id) {
		this(id, Util.getNowDateStr());
	}

	Dto(int id, String regDate) {
		this.id = id;
		this.regDate = regDate;
	}
}

class Board extends Dto {
	private String name;
	private String code;

	public Board() {
	}

	public Board(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

class Article extends Dto {
	private int boardId;
	private int memberId;
	private String title;
	private String body;

	public Article() {

	}

	public Article(int boardId, int memberId, String title, String body) {
		this.boardId = boardId;
		this.memberId = memberId;
		this.title = title;
		this.body = body;
	}

	public int getBoardId() {
		return boardId;
	}

	public void setBoardId(int boardId) {
		this.boardId = boardId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}

class ArticleReply extends Dto {
	private int id;
	private String regDate;
	private int articleId;
	private int memberId;
	private String body;

	ArticleReply() {

	}

	public int getArticleId() {
		return articleId;
	}

	public void setArticleId(int articleId) {
		this.articleId = articleId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}

class Member extends Dto {
	private String loginId;
	private String loginPw;
	private String name;

	public Member() {

	}

	public Member(String loginId, String loginPw, String name) {
		this.loginId = loginId;
		this.loginPw = loginPw;
		this.name = name;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getLoginPw() {
		return loginPw;
	}

	public void setLoginPw(String loginPw) {
		this.loginPw = loginPw;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

// Util
class Util {
	// 현재날짜문장
	public static String getNowDateStr() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = Date.format(cal.getTime());
		return dateStr;
	}

	// 파일에 내용쓰기
	public static void writeFileContents(String filePath, int data) {
		writeFileContents(filePath, data + "");
	}

	// 첫 문자 소문자화
	public static String lcfirst(String str) {
		String newStr = "";
		newStr += str.charAt(0);
		newStr = newStr.toLowerCase();

		return newStr + str.substring(1);
	}

	// 파일이 존재하는지
	public static boolean isFileExists(String filePath) {
		File f = new File(filePath);
		if (f.isFile()) {
			return true;
		}

		return false;
	}

	// 파일내용 읽어오기
	public static String getFileContents(String filePath) {
		String rs = null;
		try {
			// 바이트 단위로 파일읽기
			FileInputStream fileStream = null; // 파일 스트림

			fileStream = new FileInputStream(filePath);// 파일 스트림 생성
			// 버퍼 선언
			byte[] readBuffer = new byte[fileStream.available()];
			while (fileStream.read(readBuffer) != -1) {
			}

			rs = new String(readBuffer);

			fileStream.close(); // 스트림 닫기
		} catch (Exception e) {
			e.getStackTrace();
		}

		return rs;
	}

	// 파일 쓰기
	public static void writeFileContents(String filePath, String contents) {
		BufferedOutputStream bs = null;
		try {
			bs = new BufferedOutputStream(new FileOutputStream(filePath));
			bs.write(contents.getBytes()); // Byte형으로만 넣을 수 있음
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			try {
				bs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Json안에 있는 내용을 가져오기
	public static Object getObjectFromJson(String filePath, Class cls) {
		ObjectMapper om = new ObjectMapper();
		Object obj = null;
		try {
			obj = om.readValue(new File(filePath), cls);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public static void writeJsonFile(String filePath, Object obj) {
		ObjectMapper om = new ObjectMapper();
		try {
			om.writeValue(new File(filePath), obj);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void makeDir(String dirPath) {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
}