<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>뷰 : 08Json/login.jsp</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
</head>
<body>
<script type="text/javascript">
<!-- 제이쿼리로 변경 -->
$(function() {
	//로그인 버튼 클릭
	$('#loginBtn').click(function() {
		//폼의 빈값 체크
		var f = document.loginForm;	
		if(f.id.value==""){
			alert("아이디를 입력하세요");
			f.id.focus();
			return;
		}
		if(f.pass.value==""){
			alert("패스워드를 입력하세요"); 
			f.pass.focus();
			return;
		}
		//에이젝스 호출
		$.ajax({
			url : "./loginAction.do", //요청할 경로
			type : "post", //전송방식
			//post방식일때의 컨텐츠 타입
			contentType : "application/x-www-form-urlencoded;charset:utf-8;",
			data : { //서버로 전송할 파라미터(JSON타입)
				id : $('#id').val(),
				pass : $('#pass').val()
			},
			dataType : "json", //콜백데이터의 형식
			success : function(d) { //콜백 메소드
				/*
				콜백데이터 타입이 json이므로 별도의 파싱없이 즉시
				데이터를 읽을수 있다. 만약 json타입이 아니라면
				JSON.parse()를 호출해야 한다.
				*/
				if(d.loginResult==0){
					//로그인 실패시
					alert(d.loginMessage);
				}
				else{
					//성공시에는 list.do로 이동한다.
					alert(d.loginMessage);
					location.href='list.do';
				}
			},
			error : function(e) {
				alert("실패"+e);
			}
		});
	});
});
</script>
<div class="container"> 
	<h3>방명록(로그인)</h3> 
	
	<!-- choose  when사이엔 주석넣으면 안댕! -->
	<c:choose>
		<c:when test="${not empty sessionScope.siteUserInfo }">
		<!-- 로그인이 된 경우에는 회원의 이름과 로그아웃 버튼을 출력 -->
			<div class="row" style="border:2px solid #cccccc;padding:10px;">			
				<h4>아이디:${sessionScope.siteUserInfo.id }</h4>
				<h4>이름:${sessionScope.siteUserInfo.name }</h4>
				<br /><br />
				<button class="btn btn-danger" 
					onclick="location.href='logout.do';">
					로그아웃</button>
				&nbsp;&nbsp;
				<button class="btn btn-primary" 
					onclick="location.href='list.do';">
					방명록리스트</button>
			</div>
		</c:when>
		<c:otherwise>
			<!-- 로그아웃 상태에서는 로그인 폼을 출력한다. -->
			<span style="font-size:1.5em; color:red;">${LoginNG }</span>
			<form name="loginForm" method="post" action="./loginAction.do" onsubmit="return loginValidate(this);">
				
				<!-- 로그인에 성공할경우 이동할 페이지의 경로를 폼값으로 전송 -->
				<input type="hidden" name="backUrl" value="${param.backUrl }"/>
				
				<!-- input태그에 id속성을 부여하여 jQuery에서 선택자를 통해
				입력값을 얻어올수 있게 수정한다. -->
				<table class="table-bordered" style="width:50%;">
					<tr>
						<!-- 제이슨 사용을 위해 id 속성 추가 -->
						<td><input type="text" class="form-control" name="id" id="id" placeholder="아이디" tabindex="1"></td>
						<td rowspan="2" style="width:80px;">
						<!-- 제이슨 내에서 서브밋이 되어야 하므로 타입 버튼으로 변경 -->
						<button type="button" id="loginBtn" class="btn btn-primary" style="height:77px; width:77px;"  tabindex="3">로그인</button></td>
					</tr>
					<tr>
						<td><input type="password" class="form-control" name="pass" id="pass" placeholder="패스워드" tabindex="2"></td>
					</tr>
				</table>
			</form>
		</c:otherwise>
	</c:choose>
</div>

</body>
</html>