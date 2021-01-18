package com.kosmo.k11mybatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import mybatis.MemberVO;
import mybatis.MyBoardDTO;
import mybatis.MybatisDAOImpl;
import mybatis.MybatisMemberImpl;
import mybatis.ParameterDTO;
import util.PagingUtil;

@Controller
public class JSONController {
	
	//Mybatis를 사용하기 위한 빈을 자동주입 받음.
	@Autowired
	private SqlSession sqlSession;
	
	//방명록 게시판의 틀이 되는 페이지
	@RequestMapping("/mybatisJSON/list.do")
	public String board() 
	{
		return "08Json/board";
	}
	
	//얘 2번 누르면 List로 그냥 넘어간다규 확인하기★★★★★
	@RequestMapping("/mybatisJSON/aList.do")
	public String aList(Model model, HttpServletRequest req)
	{
		ParameterDTO parameterDTO = new ParameterDTO();
		parameterDTO.setSearchField(req.getParameter("searchField"));
//		parameterDTO.setSearchTxt(req.getParameter("searchTxt"));
//		System.out.println("검색어:"+parameterDTO.getSearchTxt());
		ArrayList<String> searchLists = null;
		if(req.getParameter("searchTxt")!=null) {
			searchLists = new ArrayList<String>();
			//검색어는 스페이스로 구분되므로 split()을 통해 문자열배열로 반환한다.
			String[] sTxtArray = req.getParameter("searchTxt").split(" ");
			//배열크기만큼 반복하면서 문자열을 컬렉션에 저장한다.
			for(String str : sTxtArray) {
				searchLists.add(str);
			}			
		}
		parameterDTO.setSearchTxt(searchLists);
		System.out.println("검색어:"+searchLists);
		
		int totalRecordCount = 
				sqlSession.getMapper(MybatisDAOImpl.class).getTotalCount(parameterDTO);
		System.out.println("totalRecordCount="+totalRecordCount);
		
		//마이바티스 내용 삽입
		//페이지 처리를 위한 설정값
		int pageSize = 4;
		int blockPage = 2;
		//전체페이지수 계산
		int totalPage = 
			(int)Math.ceil((double)totalRecordCount/pageSize);
		//현재페이지 번호 가져오기
		int nowPage = req.getParameter("nowPage")==null ? 1 :
			Integer.parseInt(req.getParameter("nowPage"));
		//select할 게시물의 구간을 계산
		int start = (nowPage-1) * pageSize + 1;
		int end = nowPage * pageSize;
		//기존의 형태와는 다르게 DTO객체에 저장한 후 Mapper를 호출한다. 
		parameterDTO.setStart(start);
		parameterDTO.setEnd(end);
		
		//Mapper 호출
		ArrayList<MyBoardDTO> lists =
			sqlSession.getMapper(MybatisDAOImpl.class)
				.listPage(parameterDTO);
		
		//MyBatis 기본쿼리출력
		String sql = sqlSession.getConfiguration()
				.getMappedStatement("listPage")
					.getBoundSql(parameterDTO).getSql();
		System.out.println("sql="+sql);
		
		//페이지번호 처리
//				String pagingImg =
//					PagingUtil.pagingImg(totalRecordCount, pageSize, blockPage, nowPage,
//						req.getContextPath()+"/mybatis/list.do?");
//				model.addAttribute("pagingImg", pagingImg);
		
		String pagingImg =
				PagingUtil.pagingAjax(totalRecordCount, pageSize, blockPage, nowPage,
						req.getContextPath()+"/mybatis/list.do?");
		model.addAttribute("pagingImg", pagingImg);
				
		//게시물의 줄바꿈 처리
		for(MyBoardDTO dto : lists)
		{
			String temp = dto.getContents().replace("\r\n", "<br/>");
			dto.setContents(temp);
		}
		model.addAttribute("lists", lists);
		
		return "08Json/aList";
	}
	
	//로그인 페이지
	@RequestMapping("/mybatisJSON/login.do")
	public String login() 
	{
		return "08Json/login";
	}
	
	//로그인 처리부분
	@RequestMapping("/mybatisJSON/loginAction.do")
	@ResponseBody
	public Map<String, Object> loginAction(HttpServletRequest req, HttpSession session) 
	{	
		//콜백데이터로 JSON을 사용하기 위해 Map컬렉션 선언
		Map<String, Object> map = new HashMap<String, Object>();
		
		//마이바티스를 통해 로그인 확인
		//Mapper에서 로그인 처리후 회원정보를 저장한 VO객체를 반환한다.
		MemberVO vo = sqlSession.getMapper(MybatisMemberImpl.class)
				.login(
				req.getParameter("id"),
				req.getParameter("pass")
		);
			
		if(vo==null) {
			//로그인 실패한 경우 0을 저장
			map.put("loginResult", 0);
			map.put("loginMessage", "로그인 실패");//login.jsp제이슨객체저장
		}
		else {
			//로그인에 성공한 경우 session영역에 VO객체 저장
			session.setAttribute("siteUserInfo", vo);
			
			map.put("loginResult", 1);
			map.put("loginMessage", "로그인 성공");
		}
		return map;
	}
	
	//로그아웃
	@RequestMapping("/mybatisJSON/logout.do")
	public String logout(HttpSession session)
	{	//null로 빈객체 되므로 로그아웃 됨
		session.setAttribute("siteUserInfo", null);
		return "redirect:login.do";
	}
	
	//삭제처리
	@RequestMapping("/mybatisJSON/deleteAction.do")
	@ResponseBody
	public Map<String, Object> delete(HttpServletRequest req, HttpSession session)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		//로그인 되었는지 확인
		if(session.getAttribute("siteUserInfo")==null) {
			map.put("statusCode", 1);//로그인이 안됐다면 반환코드1
			return map;
		}
		
		//Mybatis 사용
		int result = sqlSession.getMapper(MybatisDAOImpl.class)
				.delete(req.getParameter("idx"), 
				((MemberVO)session.getAttribute("siteUserInfo")).getId()
		);
		
		if(result<=0) {
			//삭제실패라면 반환코드 0
			map.put("statusCode", 0);
		}
		else {
			//삭제성공이면 반환코드 2
			map.put("statusCode", 2);
		}
		return map;
	}
}
