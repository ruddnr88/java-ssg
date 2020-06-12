package 선생님게시판풀이;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		Site site = new Site();

		RequestHandler requestHandler = new RequestHandler();
		requestHandler.setScanner(scanner);
		requestHandler.setSite(site);
		requestHandler.start();

		scanner.close();
	}
}

// 역할 : 고객의 요청을 받아서 컨트롤러에게 넘긴다.
class RequestHandler {
	Scanner scanner;
	Controller controller;
	Site site;

	RequestHandler() {
		controller = new Controller();
	}

	void setSite(Site site) {
		controller.setSite(site);
	}

	void start() {
		System.out.printf("== 커뮤니티 사이트 시작 ==\n");

		while (true) {
			System.out.printf("명령어) ");
			String command = scanner.nextLine();
			command = command.trim();

			if (command.length() == 0) {
				continue;
			}

			boolean needContinue = controller.route(command);

			if (needContinue == false) {
				break;
			}
		}

		System.out.printf("== 커뮤니티 사이트 끝 ==\n");
	}

	void setScanner(Scanner scanner) {
		this.scanner = scanner;
		controller.setScanner(scanner);
	}
}

// 역할 : 리퀘스트 핸들러에게 요청을 받으면 그에 맞는 적절한 명령어를 실행한다.
// 역할 : 명령이 입력되면, site, selectedBoard, loginedMember 에게 위임한다.
class Controller {
	Scanner scanner;
	Member loginedMember;
	Board selectedBoard;
	Site site;

	boolean route(String command) {
		if (command.equals("exit")) {
			doCommandSystemExit();

			return false;
		}
		if (command.equals("help")) {
			doCommandSystemHelp();

			return true;
		}

		String[] commandBits = command.split(" ");

		if (commandBits.length <= 1) {
			System.out.printf("올바르지 않은 명령어 입니다.\n");

			return true;
		}

		String contextCommand = commandBits[0];
		String actionCommand = commandBits[1];

		String arg1 = null;

		if (commandBits.length > 2) {
			arg1 = commandBits[2];
		}

		if (contextCommand.equals("member")) {
			if (actionCommand.equals("join")) {
				doCommandMemberJoin();
				return true;
			} else if (actionCommand.equals("login")) {
				doCommandMemberLogin();
				return true;
			} else if (actionCommand.equals("logout")) {
				doCommandMemberLogout();
				return true;
			}
		} else if (contextCommand.equals("article")) {
			if (actionCommand.equals("write")) {
				if (isLogined() == false) {
					System.out.printf("로그인 후 이용해주세요.\n");
					return true;
				}

				doCommandArticleWrite();
				return true;
			} else if (actionCommand.equals("modify")) {
				if (isLogined() == false) {
					System.out.printf("로그인 후 이용해주세요.\n");
					return true;
				}

				if (arg1 == null) {
					System.out.printf("게시물 번호를 입력해주세요.\n");
					return true;
				}

				int id = Integer.parseInt(arg1);
				doCommandArticleModify(id);
				return true;
			} else if (actionCommand.equals("delete")) {

				if (isLogined() == false) {
					System.out.printf("로그인 후 이용해주세요.\n");
					return true;
				}

				if (arg1 == null) {
					System.out.printf("게시물 번호를 입력해주세요.\n");
					return true;
				}

				int id = Integer.parseInt(arg1);
				doCommandArticleDelete(id);
				return true;
			} else if (actionCommand.equals("list")) {
				if (arg1 == null) {
					System.out.printf("페이지 번호를 입력해주세요.\n");
					return true;
				}

				int page = Integer.parseInt(arg1);
				doCommandArticleList(page);
				return true;
			}
		} else if (contextCommand.equals("board")) {
			if (actionCommand.equals("list")) {
				doCommandBoardList();
				return true;
			} else if (actionCommand.equals("change")) {
				if (arg1 == null) {
					System.out.printf("게시판 코드를 입력해주세요.\n");
					return true;
				}

				doCommandBoardChange(arg1);
				return true;
			}
		}

		System.out.printf("올바르지 않은 명령어 입니다.\n");

		return true;
	}

	void doCommandBoardChange(String code) {
		Board board = site.getBoardByCode(code);

		if (board == null) {
			System.out.printf("%s(은)는 존재하지 않는 게시판 코드 입니다.\n", code);
			return;
		}

		selectedBoard = board;
		System.out.printf("%s 게시판으로 변경되었습니다.\n", board.name);
	}

	void doCommandBoardList() {
		System.out.printf("%-3s|%-18s|%-8s|%-40s\n", "번호", "날짜", "코드", "이름");

		for (int i = 0; i <= site.boardsLastIndex; i++) {
			System.out.printf("%-4s|%-20s|%-10s|%-40s\n", site.boards[i].id + "번", site.boards[i].regDate,
					site.boards[i].code, site.boards[i].name);
		}
	}

	void doCommandSystemHelp() {
		System.out.printf("== 사용법 ==\n");
		System.out.printf("- help : 명령어 리스트\n");
		System.out.printf("- exit : 종료\n");
		System.out.printf("- member join : 회원가입\n");
		System.out.printf("- member login : 회원 로그인\n");
		System.out.printf("- member logout : 회원 로그아웃\n");
		System.out.printf("- board list : 게시판 리스트\n");
		System.out.printf("- board change free : 게시판 변경\n");
		System.out.printf("- article write : 게시물 작성\n");
		System.out.printf("- article list 1 : 게시물 리스트 1 페이지\n");
		System.out.printf("- article delete 1 : 1번 게시물 삭제\n");
		System.out.printf("- article modify 1 : 1번 게시물 수정\n");
	}

	void doCommandMemberLogout() {
		if (isLogined() == false) {
			System.out.printf("이미 로그아웃 상태입니다.\n");
			return;
		}

		loginedMember = null;
		System.out.printf("로그아웃 되었습니다.\n");
	}

	void doCommandMemberLogin() {
		String loginId;
		String loginPw;

		while (true) {
			System.out.printf("아이디 : ");
			loginId = scanner.nextLine();

			loginId = loginId.trim();

			if (loginId.length() == 0) {
				System.out.printf("아이디를 입력해주세요.\n");
				continue;
			}

			break;
		}

		while (true) {
			System.out.printf("비밀번호 : ");
			loginPw = scanner.nextLine();

			loginPw = loginPw.trim();

			if (loginPw.length() == 0) {
				System.out.printf("비밀번호를 입력해주세요.\n");
				continue;
			}

			break;
		}

		Member member = site.getMemberByLoginId(loginId);

		if (member == null) {
			System.out.printf("존재하지 않는 로그인 아이디 입니다.\n");
			return;
		}

		if (member.loginPw.equals(loginPw) == false) {
			System.out.printf("비밀번호가 일치하지 않습니다.\n");
			return;
		}

		loginedMember = member;
		System.out.printf("%s님 환영합니다.\n", loginedMember.name);
	}

	boolean isLogined() {
		return loginedMember != null;
	}

	void doCommandMemberJoin() {
		String loginId;
		String loginPw;
		String name;
		String email;

		while (true) {
			System.out.printf("아이디 : ");
			loginId = scanner.nextLine();

			loginId = loginId.trim();

			if (loginId.length() == 0) {
				System.out.printf("아이디를 입력해주세요.\n");
				continue;
			} else if (site.memberLoginIdHasNeverBeenUsed(loginId) == false) {
				System.out.printf("%s(은)는 이미 사용중인 아이디 입니다.\n", loginId);
				continue;
			}

			break;
		}

		while (true) {
			System.out.printf("비밀번호 : ");
			loginPw = scanner.nextLine();

			loginPw = loginPw.trim();

			if (loginPw.length() == 0) {
				System.out.printf("비밀번호를 입력해주세요.\n");
				continue;
			}

			break;
		}

		while (true) {
			System.out.printf("이름 : ");
			name = scanner.nextLine();

			name = name.trim();

			if (name.length() == 0) {
				System.out.printf("이름을 입력해주세요.\n");
				continue;
			}

			break;
		}

		while (true) {
			System.out.printf("이메일 : ");
			email = scanner.nextLine();

			email = email.trim();

			if (email.length() == 0) {
				System.out.printf("이메일을 입력해주세요.\n");
				continue;
			}

			break;
		}

		site.joinMember(loginId, loginPw, name, email);

		System.out.printf("회원가입이 완료되었습니다.\n");
	}

	void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}

	void setSite(Site site) {
		this.site = site;

		this.selectedBoard = site.getBoardByCode("notice");
	}

	void doCommandArticleList(int page) {
		int maxPageItemsCount = 2;
		Article[] articles = selectedBoard.getArticlesForPage(maxPageItemsCount, page);

		System.out.printf("%-3s|%-18s|%-40s\n", "번호", "날짜", "제목");

		for (int i = 0; i < articles.length; i++) {
			System.out.printf("%-4s|%-20s|%-40s\n", articles[i].id + "번", articles[i].regDate, articles[i].title);
		}
	}

	void doCommandArticleDelete(int id) {
		if (selectedBoard.hasArticle(id) == false) {
			System.out.printf("%d번 게시물은 존재하지 않습니다.\n", id);
		}

		Article article = selectedBoard.getArticleById(id);
		if (article.memberId != loginedMember.id) {
			System.out.printf("권한이 없습니다.\n");
		}

		selectedBoard.removeArticle(id);

		System.out.printf("%d번 게시물이 삭제되었습니다.\n", id);
	}

	void doCommandArticleModify(int id) {
		if (selectedBoard.hasArticle(id) == false) {
			System.out.printf("%d번 게시물은 존재하지 않습니다.\n", id);
			return;
		}

		Article article = selectedBoard.getArticleById(id);
		if (article.memberId != loginedMember.id) {
			System.out.printf("권한이 없습니다.\n");
		}

		System.out.printf("제목 : ");
		String title = scanner.nextLine();
		System.out.printf("내용 : ");
		String body = scanner.nextLine();

		selectedBoard.modifyArticle(id, title, body);

		System.out.printf("%d번 게시물이 수정되었습니다.\n", id);
	}

	void doCommandArticleWrite() {
		System.out.printf("제목 : ");
		String title = scanner.nextLine();
		System.out.printf("내용 : ");
		String body = scanner.nextLine();
		int memberId = loginedMember.id;

		int newId = selectedBoard.writeArticle(title, body, memberId);

		System.out.printf("%d번 게시물이 작성되었습니다.\n", newId);
	}

	void doCommandSystemExit() {
		System.out.printf("== 시스템 종료 ==\n");
	}
}

class Site {
	Board[] boards;
	int boardsLastIndex;
	int boardsLastId;
	Member[] members;
	int membersLastIndex;
	int membersLastId;

	Site() {
		boardsLastIndex = -1;
		boardsLastId = 0;
		boards = new Board[100];

		addBoard("공지사항", "notice");
		addBoard("자유게시판", "free");

		membersLastIndex = -1;
		membersLastId = 0;
		members = new Member[100];

		int member1Id = addMember("user1", "user1", "유저1", "user1@test.com");
		int member2Id = addMember("user2", "user2", "유저2", "user2@test.com");

		Board noticeBoard = getBoardByCode("notice");

		noticeBoard.writeArticle("공지사항 글", "공지사항 글 내용", member1Id);

		Board freeBoard = getBoardByCode("free");

		freeBoard.writeArticle("자유게시판 글", "자유게시판 글 내용", member2Id);
	}

	Member getMemberByLoginId(String loginId) {
		int index = getMemberIndexByLoginId(loginId);

		if (index >= 0) {
			return members[index];
		}

		return null;
	}

	int getMemberIndexByLoginId(String loginId) {
		for (int i = 0; i <= membersLastIndex; i++) {
			if (members[i].loginId.equals(loginId)) {
				return i;
			}
		}
		return -1;
	}

	void joinMember(String loginId, String loginPw, String name, String email) {
		int id = membersLastId + 1;
		membersLastId++;
		String regDate = Util.getNowDateStr();

		Member member = new Member();

		member.id = id;
		member.regDate = regDate;
		member.loginId = loginId;
		member.loginPw = loginPw;
		member.name = name;
		member.email = email;

		membersLastIndex++;
		members[membersLastIndex] = member;
	}

	boolean memberLoginIdHasNeverBeenUsed(String loginId) {
		int index = getMemberIndexByLoginId(loginId);
		return index == -1;
	}

	Board getBoardByCode(String code) {
		for (int i = 0; i <= boardsLastIndex; i++) {
			if (boards[i].code.equals(code)) {
				return boards[i];
			}
		}

		return null;
	}

	int addMember(String loginId, String loginPw, String name, String email) {
		int id = membersLastId + 1;
		membersLastId++;
		String regDate = Util.getNowDateStr();

		Member member = new Member();
		member.id = id;
		member.regDate = regDate;
		member.loginId = loginId;
		member.loginPw = loginPw;
		member.name = name;
		member.email = email;

		membersLastIndex++;
		members[membersLastIndex] = member;

		return id;
	}

	int addBoard(String name, String code) {
		int id = boardsLastId + 1;
		boardsLastId++;
		String regDate = Util.getNowDateStr();

		Board board = new Board();

		board.id = id;
		board.regDate = regDate;
		board.name = name;
		board.code = code;

		boardsLastIndex++;
		boards[boardsLastIndex] = board;

		return id;
	}

	Article[] getSelectedBoardArticles() {

		return null;
	}

}

class Board {
	int id;
	String regDate;
	String name;
	String code;
	Article[] articles;
	int articlesLastIndex;
	int articlesLastId;

	Board() {
		articles = new Article[100];
		articlesLastIndex = -1;
		articlesLastId = 0;
	}

	void modifyArticle(int id, String title, String body) {
		Article article = getArticleById(id);
		article.title = title;
		article.body = body;
	}

	void removeArticle(int id) {
		int index = getIndexOfArticle(id);

		for (int i = index; i < articlesLastIndex; i++) {
			articles[i] = articles[i + 1];
		}

		articlesLastIndex--;
	}

	int getIndexOfArticle(int id) {
		for (int i = 0; i <= articlesLastIndex; i++) {
			if (articles[i].id == id) {
				return i;
			}
		}
		return -1;
	}

	Article getArticleById(int id) {
		int index = getIndexOfArticle(id);

		return articles[index];
	}

	boolean hasArticle(int id) {
		return getIndexOfArticle(id) != -1;
	}

	int writeArticle(String title, String body, int memberId) {
		int id = articlesLastId + 1;
		articlesLastId++;
		String regDate = Util.getNowDateStr();

		Article article = new Article();

		article.id = id;
		article.regDate = regDate;
		article.title = title;
		article.body = body;
		article.memberId = memberId;

		articlesLastIndex++;
		articles[articlesLastIndex] = article;

		return id;
	}

	Article[] getArticlesForPage(int maxPageItemsCount, int page) {
		if (articlesLastIndex == -1) {
			return new Article[0];
		}

		int startPos = maxPageItemsCount * (page - 1);
		int endPos = startPos + maxPageItemsCount - 1;

		if (endPos > articlesLastIndex) {
			endPos = articlesLastIndex;
		}

		int pageItemsCount = endPos - startPos + 1;

		if (pageItemsCount < 0) {
			return new Article[0];
		}

		Article[] articlesForPaging = new Article[pageItemsCount];
		int articlesIndex = -1;

		for (int i = startPos; i <= endPos; i++) {
			articlesIndex++;
			articlesForPaging[articlesIndex] = articles[i];
		}

		return articlesForPaging;
	}

	int getArticlesCount() {
		return articlesLastIndex + 1;
	}
}

class Article {
	int id;
	String regDate;
	int boardId;
	int memberId;
	String title;
	String body;
	ArticleReply[] articleReplies;
	int articleRepliesLastIndex;
}

class ArticleReply {
	int id;
	String regDate;
	int memberId;
	String title;
	String body;
}

class Member {
	int id;
	String regDate;
	String loginId;
	String loginPw;
	String name;
	String email;
}

class Util {
	static String getNowDateStr() {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar time = Calendar.getInstance();
		String dateStr = format1.format(time.getTime());

		return dateStr;
	}
}
