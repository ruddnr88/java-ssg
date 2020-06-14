//오류 : 리스트 페이징/ 삭제안됨/ 리스트 작성자에 로그인된사람 나옴/저장파일에 패키지이름 붙음
//보드리스트,보드변경

package pair3;

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
	private static BoardService boardService;
	private static BoardDao boardDao;
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

	public static BoardService getBoardService() {
		if (boardService == null) {
			boardService = new BoardService();
		}

		return boardService;

	}

	public static BoardDao getBoardDao() {
		if (boardDao == null) {
			boardDao = new BoardDao();
		}

		return boardDao;
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
		controllers.put("board", new BoardController());
	}

	public App() {
		// 컨트롤러 등록
		initControllers();

		// 관리자 회원 생성
		Factory.getMemberService().join("admin", "admin", "관리자");

		// 공지사항 게시판 생성
		Factory.getArticleService().makeBoard("공지시항", "notice");
		// 자유 게시판 생성
		Factory.getArticleService().makeBoard("자유게시판", "free");

		// 현재 게시판을 1번 게시판으로 선택
		Factory.getSession().setCurrentBoard(Factory.getArticleService().getBoard(1));
		// 임시 : 현재 로그인 된 회원은 1번 회원으로 지정, 이건 나중에 회원가입, 로그인 추가되면 제거해야함
		// Factory.getSession().setLoginedMember(Factory.getMemberService().getMember(1));
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

class BoardController extends Controller {
	private BoardService boardService;

	BoardController() {
		boardService = Factory.getBoardService();
	}

	void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("list")) {
			actionBoardList(reqeust);
		} else if (reqeust.getActionName().equals("change")) {
			actionBoardChange(reqeust);
		} else if (reqeust.getActionName().equals("delete")) {
//			 actionBoardDelete(reqeust);
		}
	}

	private void actionBoardChange(Request reqeust) {
		Board board = boardService.getBodardChange(reqeust.getArg1());

		if (board == null) {
			System.out.printf("존재하지 않는 게시판입니다.\n");
			return;
		}

		System.out.printf("%s 게시판으로 변경되었습니다.\n", board.getName());
	}

	private void actionBoardList(Request reqeust) {
		List<Board> boards = boardService.getBoards();
		System.out.printf("%-3s|%-18s|%-8s|%-40s\n", "번호", "날짜", "코드", "이름");
		for (int i = 0; i < boards.size(); i++) {
			System.out.printf("%-4s|%-20s|%-10s|%-40s\n", boards.get(i).getId() + "번", boards.get(i).getRegDate(),
					boards.get(i).getCode(), boards.get(i).getName());
		}
	}
}

class ArticleController extends Controller {
	private ArticleService articleService;

	ArticleController() {
		articleService = Factory.getArticleService();
	}

	public void doAction(Request reqeust) {
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
		}
	}

	private void actionDetail(Request reqeust) {
		if (Factory.getSession().getLoginedMember() == null) {
			System.out.println("로그인 후 이용가능");
			return;
		}
		int number = Integer.parseInt(reqeust.getArg1());
		articleService.detailArticle(number);
	}

	private void actionDelete(Request reqeust) {
		if (Factory.getSession().getLoginedMember() == null) {
			System.out.println("로그인 후 이용가능");
			return;
		}
		System.out.println("== 게시물 삭제 ==");
		int number = Integer.parseInt(reqeust.getArg1());
		articleService.deleteArticle(number);

	}

//아래 초록이 하고싶다.
	private void actionModify(Request reqeust) {
		if (Factory.getSession().getLoginedMember() == null) {
			System.out.println("로그인 후 이용가능");
			return;
		}

		System.out.println("== 게시물 수정 == ");
		int number = Integer.parseInt(reqeust.getArg1());

		int result = articleService.isExistArticle(number);

		if (number == 0) {
			System.out.println("게시글 번호를 입력해 주세요.");
			return;
		}

		if (result > 0) {
			System.out.println("== 게시글 수정 ==");
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
		return;

	}

	// 작성자에 현재로그인 한 사람 나옴 수정!!
	private void actionList(Request reqeust) {
		List<Article> articles = articleService.getArticles();
		System.out.println("== 게시물 리스트 ==");
		System.out.printf("%-3s|%-18s|%-7s|%-38s|%-7s\n", "번호", "날짜", "제목", "내용", "작성자");
		for (int i = 0; i < articles.size(); i++) {
			System.out.printf("%-5s|%-20s|%-9s|%-40s|%-7s\n", articles.get(i).getId(), articles.get(i).getRegDate(),
					articles.get(i).getTitle(), articles.get(i).getBody(),
					Factory.getSession().getLoginedMember().getName());
		}
	}

	private void actionWrite(Request reqeust) {
		if (Factory.getSession().getLoginedMember() == null) {
			System.out.println("로그인 후 이용가능");
			return;
		}
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

class MemberController extends Controller {
	private MemberService memberService;

	MemberController() {
		memberService = Factory.getMemberService();
	}

	void doAction(Request reqeust) {
		if (reqeust.getActionName().equals("logout")) {
			actionLogout(reqeust);
		} else if (reqeust.getActionName().equals("login")) {
			actionLogin(reqeust);
		} else if (reqeust.getActionName().equals("whoami")) {
			actionWhoami(reqeust);
		} else if (reqeust.getActionName().equals("join")) {
			actionJoin(reqeust);
		}
	}

	private void actionJoin(Request reqeust) {

		boolean loginedMember = Factory.getSession().isLogined();

		if (loginedMember == true) {
			System.out.println("현재 로그인 상태입니다.");
			return;

		} else {
			System.out.println("== 회원가입 시작 ==");

			String name;
			String loginId;
			String loginPw;
			String loginPwConfirm;

			while (true) {
				System.out.printf("이름 : ");
				name = Factory.getScanner().nextLine().trim();
				if (name.length() == 0) {
					System.out.println("이름을 입력해주세요.");
					continue;
				}
				if (name.length() < 2) {
					System.out.println("이름을 2자 이상 입력해주세요.");
					continue;
				}
				break;
			}
			while (true) {
				System.out.printf("아이디 : ");
				loginId = Factory.getScanner().nextLine().trim();
				if (loginId.length() == 0) {
					System.out.println("아이디를 입력해주세요.");
					continue;
				}
				if (loginId.length() < 2) {
					System.out.println("아이디를 2자 이상 입력해주세요.");
					continue;
				}

				if (memberService.isUsedLoginId(loginId)) {
					System.out.printf("입력하신 아이디 [%s]는 이미 사용중입니다.\n", loginId);
					continue;
				}
				break;
			}

			while (true) {
				boolean loginPwValid = true;
				// 비밀번호 확인시 틀리면 다시 처음부터 입력되게 해야하는 함수.

				while (true) {
					System.out.printf("비밀번호 : ");
					loginPw = Factory.getScanner().nextLine().trim();
					if (loginPw.length() == 0) {
						System.out.println("비밀번호를 입력해주세요.");
						continue;
					}
					if (loginPw.length() < 2) {
						System.out.println("비밀번호를 2자 이상 입력해주세요.");
						continue;
					}
					break;

				}
				while (true) {
					System.out.printf("비밀번호 확인 : ");
					loginPwConfirm = Factory.getScanner().nextLine().trim();
					if (loginPwConfirm.length() == 0) {
						System.out.println("비밀번호 확인을 입력해주세요.");
						continue;
					}
					if (loginPw.equals(loginPwConfirm) == false) {
						System.out.println("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
						loginPwValid = false;
						break;
					}
					break;
				}

				if (loginPwValid) {
					break;
				}
			}

			int rs = memberService.join(name, loginId, loginPw);

			if (rs < 1) {
				System.out.println("입력하신 로그인 아이디는 이미 사용중입니다..");
			} else {
				System.out.printf("회원가입을 축하합니다!\n당신의 아이디는 (%s) 입니다.\n", loginId);
			}

			System.out.printf("== 회원가입 끝 ==\n");
		}

	}

	private void actionWhoami(Request reqeust) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember == null) {
			System.out.println("비회원입니다.");
		} else {
			System.out.println("====== 회원정보 ======");
			System.out.println("이름 : " + loginedMember.getName() + ", 아이디 :" + loginedMember.getLoginId());
		}

	}

	private void actionLogin(Request reqeust) {

		boolean loginedMember = Factory.getSession().isLogined();

		if (loginedMember == true) {
			System.out.println("현재 로그인 상태입니다.");
			return;

		} else {
			System.out.printf("로그인 아이디 : ");
			String loginId = Factory.getScanner().nextLine().trim();

			System.out.printf("로그인 비번 : ");
			String loginPw = Factory.getScanner().nextLine().trim();

			Member member = memberService.getMemberByLoginIdAndLoginPw(loginId, loginPw);

			if (member == null) {
				System.out.println("일치하는 회원이 없습니다.");
			} else {
				System.out.println(member.getName() + "님 환영합니다.");
				Factory.getSession().setLoginedMember(member);
			}
		}
	}

	private void actionLogout(Request reqeust) {
		Member loginedMember = Factory.getSession().getLoginedMember();

		if (loginedMember != null) {
			Session session = Factory.getSession();
			System.out.println("로그아웃 되었습니다.");
			session.setLoginedMember(null);
		}

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
			// list의 ${TR}에 html 들어감
			html = template.replace("${TR}", html);

			html = head + html + foot;

			Util.writeFileContents("site/article/" + fileName, html);
		}

		// 게시물 별 파일 생성
		List<Article> articles = articleService.getArticles();

		for (Article article : articles) {
			String html = "<html>";

			html += "<head>";
			html += "<meta charset=\"utf-8\">";
			html += "</head>";

			html += "<body>";

			html += "<div>제목 : " + article.getTitle() + "</div>";
			html += "<div>내용 : " + article.getBody() + "</div>";
			html += "<div><a href=\"" + (article.getId() - 1) + ".html\">이전글</a></div>";
			html += "<div><a href=\"" + (article.getId() + 1) + ".html\">다음글</a></div>";

			html += "</body>";

			html += "</html>";

			Util.writeFileContents("site/article/" + article.getId() + ".html", html);
		}
	}

}

class BoardService {
	private BoardDao boardDao;

	BoardService() {
		boardDao = Factory.getBoardDao();
	}

	public Board getBodardChange(String arg1) {
		return boardDao.getBoardChange(arg1);
	}

	public List<Board> getBoards() {
		return boardDao.getBoards();
	}
}

class ArticleService {
	private ArticleDao articleDao;

	ArticleService() {
		articleDao = Factory.getArticleDao();
	}

	public void detailArticle(int number) {
		articleDao.detailArticle(number);

	}

	public List<Article> getArticlesByBoardCode(String code) {
		return articleDao.getArticlesByBoardCode(code);
	}

	public List<Board> getBoards() {
		return articleDao.getBoards();
	}

	public void deleteArticle(int number) {
		articleDao.deleteArticle(number);
	}

	public void modifyArticle(int number, String title, String body) {
		articleDao.modifyArticle(number, title, body);

	}

	public int isExistArticle(int number) {
		return articleDao.isExistArticle(number);
	}

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

	public boolean isUsedLoginId(String loginId) {
		Member member = memberDao.getMemberByLoginId(loginId);

		if (member == null) {
			return false;
		}
		return true;
	}

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		return memberDao.getMemberByLoginIdAndLoginPw(loginId, loginPw);
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
class BoardDao {
	DB db;

	BoardDao() {
		db = Factory.getDB();
	}

	

	public Board getBoardChange(String arg1) {
		return db.getBoardByCode(arg1);
	}



	public List<Board> getBoards() {
		return db.getBoards();
	}
}

class ArticleDao {
	DB db;

	ArticleDao() {
		db = Factory.getDB();
	}

	public void detailArticle(int number) {
		db.detailArticle(number);
	}

	public List<Article> getArticlesByBoardCode(String code) {
		return db.getArticlesByBoardCode(code);
	}

	public List<Board> getBoards() {
		return db.getBoards();
	}

	public void deleteArticle(int number) {
		db.deleteArticle(number);
	}

	public void modifyArticle(int number, String title, String body) {
		db.modifyArticle(number, title, body);

	}

	public int isExistArticle(int number) {
		return db.isExistArticle(number);
	}

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

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		return db.getMemberByLoginIdAndLoginPw(loginId, loginPw);
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

	public void detailArticle(int number) {
		Article a = getArticleById(number);
		if (a != null) {
			System.out.println(a.getId() + "번 게시물 상세보기");
			System.out.println("제목 : " + a.getTitle());
			System.out.println("내용 : " + a.getBody());
			System.out.println("작성자 : " + getMember(a.getMemberId()).getName());
		} else {
			System.out.println("존재하지 않는 게시물입니다.");
		}
	}

	public List<Article> getArticlesByBoardCode(String code) {
		Board board = getBoardByCode(code);
		// free => 2
		// notice => 1

		List<Article> articles = getArticles();
		List<Article> newArticles = new ArrayList<>();

		for (Article article : articles) {
			if (article.getBoardId() == board.getId()) {
				newArticles.add(article);
			}
		}

		return newArticles;
	}

	public void deleteArticle(int number) {
		File file = new File(getDirPath() + "/copy.article/" + number + ".json");
		if (file.exists()) {
			Article article = getArticleById(number);
			if (article.getMemberId() == Factory.getSession().getLoginedMember().getId()) {
				file.delete();
				System.out.println(number + "번 게시글 삭제완료.");
			} else {
				System.out.println("게시글 삭제는 작성자 본인만 가능합니다.");
			}
		}
	}

	public void modifyArticle(int number, String title, String body) {
		Article oldArticle = getArticleById(number);
		Article article = new Article(oldArticle.getBoardId(), oldArticle.getMemberId(), title, body);
		article.setId(number);
		// regDate는 수정되는 시간으로 변경

		tables.get("article").saveRow(article);

	}

	private Article getArticleById(int num) {
		List<Article> articles = getArticles();
		for (Article a : articles) {
			if (a.getId() == num) {
				return a;
			}
		}
		return null;
	}

	public int isExistArticle(int number) {
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

	public Member getMemberByLoginIdAndLoginPw(String loginId, String loginPw) {
		List<Member> members = getMembers();

		for (Member member : members) {
			if (member.getLoginId().equals(loginId) && member.getLoginPw().equals(loginPw)) {
				return member;
			}
		}

		return null;
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
		this.tableName = Util.lcfirst(dataCls.getCanonicalName());
		this.tableDirPath = dbDirPath + "/" + this.tableName;

		Util.makeDir(tableDirPath);
	}

	private String getTableName() {
		return tableName;
	}

	public int saveRow(T data) {
		Dto dto = (Dto) data;

		if (dto.getId() == 0) {
			int lastId = getLastId();
			int newId = lastId + 1;
			dto.setId(newId);
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