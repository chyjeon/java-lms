package nextstep.sessions.domain;

import nextstep.money.Money;
import nextstep.payments.domain.Payment;
import nextstep.payments.exception.InvalidPaymentException;
import nextstep.payments.exception.PaymentAmountMismatchException;
import nextstep.sessions.exception.InvalidSessionException;
import nextstep.sessions.exception.InvalidSessionJoinException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static nextstep.payments.domain.PaymentState.PAYMENT_COMPLETE;
import static nextstep.sessions.domain.SessionState.FINISHED;
import static nextstep.sessions.domain.SessionState.PREPARING;
import static nextstep.sessions.domain.SessionState.RECRUITING;
import static nextstep.users.domain.NsUserTest.JAVAJIGI;
import static nextstep.users.domain.NsUserTest.SANJIGI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionTest {

    @Test
    void create() {
        LocalDateTime now = LocalDateTime.now();
        Session session = Session.builder().state(PREPARING).createdAt(now).build();
        assertThat(session).isEqualTo(Session.builder().state(PREPARING).createdAt(now).build());
    }

    @Test
    void 강의시작일이_종료일이후이면_예외를_던진다() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime startDate = LocalDateTime.of(2024, 4, 2, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 4, 1, 23, 59, 59);

        assertThatThrownBy(() -> Session.builder().state(PREPARING).sessionType(new FreeSession()).startDate(startDate).endDate(endDate).createdAt(createdAt).build())
                .isInstanceOf(InvalidSessionException.class)
                .hasMessage("시작일이 종료일보다 이전이어야 합니다");
    }

    @DisplayName("수강 신청 테스트")
    @Nested
    class SessionRequestJoinNestedTest {
        @ParameterizedTest
        @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = {"RECRUITING"})
        void 강의상태가_모집중이_아닌경우_신청불가하다(SessionState sessionState) {
            Session session = Session.builder().state(sessionState).build();
            assertThatThrownBy(() -> session.requestJoin(JAVAJIGI, LocalDateTime.now()))
                    .isInstanceOf(InvalidSessionJoinException.class);
        }

        @Test
        void 모집종료된_무료강의는_신청불가하다() {
            Session session = Session.builder()
                    .state(FINISHED)
                    .sessionType(new FreeSession())
                    .build();
            assertThatThrownBy(() -> session.requestJoin(JAVAJIGI, LocalDateTime.now()))
                    .isInstanceOf(InvalidSessionJoinException.class);
        }

        @Test
        void 모집중인_유료강의가_수강인원을_초과한_경우_신청할수없다() {
            Session session = Session.builder()
                    .state(RECRUITING)
                    .sessionType(new PaidSession(1, Money.wons(1000L)))
                    .build();
            session.addListener(JAVAJIGI);

            assertThatThrownBy(() -> session.requestJoin(SANJIGI, LocalDateTime.now()))
                    .isInstanceOf(InvalidSessionJoinException.class);
        }

        @Test
        void 수강신청기간을_벗어난경우_예외를던진다() {
            LocalDateTime startDate = LocalDateTime.of(2024, 4, 2, 0, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 4, 3, 23, 59, 59);
            Session session = Session.builder()
                    .state(RECRUITING)
                    .sessionType(new FreeSession())
                    .startDate(startDate)
                    .endDate(endDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            // 시작 전
            assertThatThrownBy(() -> session.requestJoin(JAVAJIGI, LocalDateTime.of(2024, 4, 1, 23, 59, 59)))
                    .isInstanceOf(InvalidSessionJoinException.class)
                    .hasMessage("현재 수강 신청 불가능한 기간 입니다");

            // 종료 후
            assertThatThrownBy(() -> session.requestJoin(JAVAJIGI, LocalDateTime.of(2024, 4, 4, 0, 0, 0)))
                    .isInstanceOf(InvalidSessionJoinException.class)
                    .hasMessage("현재 수강 신청 불가능한 기간 입니다");
        }

        @Test
        void 수강신청가능한경우_주문서를생성한다() {
            LocalDateTime startDate = LocalDateTime.of(2024, 4, 2, 0, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 4, 3, 23, 59, 59);
            Session session = Session.builder()
                    .state(RECRUITING)
                    .sessionType(new FreeSession())
                    .startDate(startDate)
                    .endDate(endDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            LocalDateTime now = LocalDateTime.of(2024, 4, 2, 12, 0, 0);
            Payment payment = session.requestJoin(JAVAJIGI, now);

            assertThat(payment).isEqualTo(new Payment(0L, 1L, Money.ZERO, now));
        }
    }

    @DisplayName("수강 등록 테스트")
    @Nested
    class SessionJoinNestedTest {
        @Test
        void 로그인유저가_널인경우_예외를_던진다() {
            Session session = Session.builder().state(RECRUITING).build();
            assertThatThrownBy(() -> session.join(null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 결제정보가_널인경우_예외를_던진다() {
            Session session = Session.builder().state(RECRUITING).build();
            assertThatThrownBy(() -> session.join(JAVAJIGI, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 이미_등록된_수강자인경우_예외를_던진다() {
            Session session = Session.builder().state(RECRUITING).sessionType(new FreeSession()).build();
            session.addListener(JAVAJIGI);

            Payment payment = new Payment(0L, JAVAJIGI.getId(), Money.wons(1000L));

            assertThatThrownBy(() -> session.join(JAVAJIGI, payment))
                    .isInstanceOf(InvalidPaymentException.class);
        }

        @Test
        void 결제완료상태가_아닌경우_예외를던진다() {
            Session session = Session.builder().state(RECRUITING).sessionType(new FreeSession()).build();
            Payment payment = new Payment(0L, JAVAJIGI.getId(), Money.ZERO);

            assertThatThrownBy(() -> session.join(JAVAJIGI, payment))
                    .isInstanceOf(InvalidPaymentException.class);
        }

        @Test
        void 강의아이디가_일치하지_않는경우_예외를던진다() {
            Session session = Session.builder().state(RECRUITING).sessionType(new FreeSession()).build(); // 0L
            Payment payment = new Payment(1L, JAVAJIGI.getId(), Money.ZERO, PAYMENT_COMPLETE);

            assertThatThrownBy(() -> session.join(JAVAJIGI, payment))
                    .isInstanceOf(InvalidPaymentException.class);
        }

        @Test
        void 유저아이디가_일치하지_않는경우_예외를던진다() {
            Session session = Session.builder().state(RECRUITING).sessionType(new FreeSession()).build();
            Payment payment = new Payment(0L, SANJIGI.getId(), Money.ZERO, PAYMENT_COMPLETE);

            assertThatThrownBy(() -> session.join(JAVAJIGI, payment))
                    .isInstanceOf(InvalidPaymentException.class);
        }

        @Test
        void 강의금액이_일치하지_않는경우_예외를던진다() {
            Session session = Session.builder()
                    .state(RECRUITING)
                    .sessionType(new PaidSession(10, Money.wons(1000L)))
                    .build();
            Payment payment = new Payment(0L, JAVAJIGI.getId(), Money.ZERO, PAYMENT_COMPLETE);

            assertThatThrownBy(() -> session.join(JAVAJIGI, payment))
                    .isInstanceOf(PaymentAmountMismatchException.class);
        }

        @Test
        void 정상적으로_수강등록시_카운트가_증가한다() {
            Session session = Session.builder()
                    .state(RECRUITING)
                    .sessionType(new PaidSession(10, Money.wons(1000L)))
                    .build();
            
            Payment payment = new Payment(0L, JAVAJIGI.getId(), Money.wons(1000L), PAYMENT_COMPLETE);
            session.join(JAVAJIGI, payment);

            assertThat(session.getCount()).isEqualTo(1);
        }
    }
}
