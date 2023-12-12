# 학습 관리 시스템(Learning Management System)
## 진행 방법
* 학습 관리 시스템의 수강신청 요구사항을 파악한다.
* 요구사항에 대한 구현을 완료한 후 자신의 github 아이디에 해당하는 브랜치에 Pull Request(이하 PR)를 통해 코드 리뷰 요청을 한다.
* 코드 리뷰 피드백에 대한 개선 작업을 하고 다시 PUSH한다.
* 모든 피드백을 완료하면 다음 단계를 도전하고 앞의 과정을 반복한다.

## 요구사항
* 기능이 일부 변경되었다.
* 강의 수강신청은 강의 상태가 모집중일 때만 가능하다.
  * 강의가 진행 중인 상태에서도 수강신청이 가능해야 한다.
* 강의 진행 상태(준비중, 진행중, 종료)와 모집 상태(비모집중, 모집중)로 상태 값을 분리해야 한다.
  * 강의는 강의 커버 이미지 정보를 가진다.
  * 강의는 하나 이상의 커버 이미지를 가질 수 있다.
### 과정
* [X] 과정은 여러 개의 강의를 가질 수 있다.
### 강의
* [X] 강의는 id, 시작일, 종료일을 가진다.
* [X] 강의 상태를 가진다.
  * [X] 강의 상태는 준비중, 진행중, 종료 3가지 상태를 가진다.
  * [X] 모집상태는 비모집중, 모집중 2가지 상태를 가진다.
  * [X] 모집상태가 모집중일 때 수강 신청이 가능하다.
* [X] 강의는 무료 강의와 유료 강의로 나뉜다.
  * [X] 무료 강의는 최대 수강 인원 제한이 없다.
  * [X] 유료 강의는 강의 최대 수강 인원을 초과할 수 없다.
  * [X] 유료 강의는 수강생이 결제한 금액과 수강료가 일치할 때 수강 신청이 가능하다.
* [X] 강의는 커버 이미지를 하나 이상 가진다.
* [X] DB를 생성한다.
* [X] DB 테이블과 매핑한다.
* [X] 강사가 승인하지 않아도 수강 신청하는 모든 사람이 수강 가능하다.
  * [ ] 우아한테크코스(무료), 우아한테크캠프 Pro(유료)와 같이 선발된 인원만 수강 가능해야 한다.
    * [ ] 강사는 수강신청한 사람 중 선발된 인원에 대해서만 수강 승인이 가능해야 한다.
    * [ ] 강사는 수강신청한 사람 중 선발되지 않은 사람은 수강을 취소할 수 있어야 한다.
    * [ ] 상태는 대기, 수락, 거절 3가지 상태를 가진다.
### 이미지
* [X] 이미지는 1MB 이하여야 한다.
* [X] 이미지 타입은 gif, jpg(jpeg 포함), png, svg만 허용한다.
* [X] 이미지의 width는 300픽셀, height는 200픽셀 이상이어야 하며, width와 height의 비율은 3:2여야 한다.
* [X] DB를 생성한다.
* [X] DB 테이블과 매핑한다.

## 수강 신청 기능 요구사항
* 과정(Course)은 기수 단위로 운영하며, 여러 개의 강의(Session)를 가질 수 있다.
* 강의는 시작일과 종료일을 가진다.
* 강의는 강의 커버 이미지 정보를 하나 이상 가진다.
  * 이미지 크기는 1MB 이하여야 한다.
  * 이미지 타입은 gif, jpg(jpeg 포함),, png, svg만 허용한다.
  * 이미지의 width는 300픽셀, height는 200픽셀 이상이어야 하며, width와 height의 비율은 3:2여야 한다.
* 강의는 무료 강의와 유료 강의로 나뉜다.
  * 무료 강의는 최대 수강 인원 제한이 없다.
  * 유료 강의는 강의 최대 수강 인원을 초과할 수 없다.
  * 유료 강의는 수강생이 결제한 금액과 수강료가 일치할 때 수강 신청이 가능하다.
* 강의 상태는 준비중, 진행중, 종료 3가지와 모집중, 모집종료 상태를 가진다.
* 강의 수강신청은 강의 상태가 모집중일 때만 가능하다.
* 유료 강의의 경우 결제는 이미 완료한 것으로 가정하고 이후 과정을 구현한다.
  * 결제를 완료한 결제 정보는 payments 모듈을 통해 관리되며, 결제 정보는 Payment 객체에 담겨 반한된다.
* 우아한테크코스(무료), 우아한테크캠프 Pro(유료)와 같이 선발된 인원만 수강 가능해야 한다.
  * 강사는 수강신청한 사람 중 선발된 인원에 대해서만 수강 승인이 가능해야 한다.
  * 강사는 수강신청한 사람 중 선발되지 않은 사람은 수강을 취소할 수 있어야 한다.

## 객체지향 생활 체조 원칙
* 규칙 1: 한 메서드에 오직 한 단계의 들여쓰기만 한다.
* 규칙 2: else 예약어를 쓰지 않는다.
* 규칙 3: 모든 원시값과 문자열을 포장한다.
* 규칙 4: 한 줄에 점을 하나만 찍는다.
* 규칙 5: 줄여쓰지 않는다(축약 금지).
* 규칙 6: 모든 엔티티를 작게 유지한다.
* 규칙 7: 3개 이상의 인스턴스 변수를 가진 클래스를 쓰지 않는다.
* 규칙 8: 일급 콜렉션을 쓴다.
* 규칙 9: 게터/세터/프로퍼티를 쓰지 않는다.