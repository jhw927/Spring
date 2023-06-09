package studt.datajpa.repository;

import org.apache.tomcat.util.descriptor.web.XmlEncodingBase;
import org.assertj.core.api.Assertions;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import studt.datajpa.dto.MemberDto;
import studt.datajpa.entity.Member;
import studt.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);


    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);
        // 단건 조회 검
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트
        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        //카운트
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        assertThat(memberRepository.count()).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGraterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> findMember = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(findMember.get(0).getUsername()).isEqualTo("AAA");
        assertThat(findMember.get(0).getAge()).isEqualTo(20);
        assertThat(findMember.size()).isEqualTo(1);
    }

    @Test
    public void namedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> findMember = memberRepository.findByUsername("AAA");

        assertThat(findMember.get(0).getUsername()).isEqualTo("AAA");
        assertThat(findMember.get(0).getAge()).isEqualTo(10);
        assertThat(findMember.size()).isEqualTo(2);
    }

    @Test
    public void teatQuery() {
        Member m1 = new Member("AAA", 10);
        memberRepository.save(m1);

        List<Member> findMember = memberRepository.findMember("AAA", 10);
        assertThat(findMember.get(0).getUsername()).isEqualTo("AAA");
        assertThat(findMember.get(0)).isEqualTo(m1);
    }

    @Test
    public void uesrnameList() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();
        for (String member : result) {
            System.out.println("member = " + member);

        }
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);
        Member m1 = new Member("aaa", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
        assertThat(memberDto.get(0).getUsername()).isEqualTo("aaa");
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaa", "bbb"));
        for (Member member : result) {
            System.out.println("member = " + member);

        }
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void returnType() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findListByUsername("aaa"); // 매핑되는 값이 없을경우 nulL이 아닌 empty 컬렉션을 반환한다.
        System.out.println("aaa = " + aaa);
        Member aaa1 = memberRepository.findMemberByUsername("aaa");
        System.out.println("aaa1 = " + aaa1);
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("aaa");
        System.out.println("aaa2 = " + aaa2);
    }

    @Test
    public void paging() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();


        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);
        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0); // 40 -> 41
        System.out.println("member5 = " + member5); //  벌크 연산시에는 꼭 영속성 컨텍스트를 비워줘야한다. 디비에는 변경이 반영되었더라도 벌크쿼리는 영속성 컨텍스트를 무시한다.

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA); // 영속성 컨텍스트에 저장
        teamRepository.save(teamB); // 영속성 컨텍스트에 저장
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1); // 영속성 컨텍스트에 저장
        memberRepository.save(member2); // 영속성 컨텍스트에 저장

        em.flush();
        em.clear();

        //when N + 1 문제
//        List<Member> members = memberRepository.findMemberFetchJoin();
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass()); // 프록시 객체
            System.out.println("member.team = " + member.getTeam().getName()); // 실제 객체
        }
    }

    @Test
    public void quertHint() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);

        em.flush();
        em.clear();

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2"); // 변경감지가 되지 않는다.

        em.flush();
    }

    @Test
    public void lock() {
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);

        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByUsername("member1");
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void JpaEventBaseEntity() throws InterruptedException {
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1); // @PrePersist

        Thread.sleep(100);
        member1.setUsername("member2");

        em.flush();// @PreUpdate
        em.clear();
        //when
        Member findMember = memberRepository.findById(member1.getId()).get();
        //then
        findMember.getCreatedDate();
        System.out.println("findMember.getUpdatedDate() = " + findMember.getUpdatedDate());
        findMember.getUpdatedDate();
        System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
    }

    @Test
    public void specBasic() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();
        //when
        Specification<Member> spec =
                MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);
        //then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void basic() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        //when
        //Probe 생성
        Member member = new Member("m1");
        Team team = new Team("teamA"); //내부조인으로 teamA 가능
        member.setTeam(team);
        //ExampleMatcher 생성, age 프로퍼티는 무시
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void projection() {

        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);
        em.flush();
        em.clear();
//        //when
//        List<UsernameOnly> result =
//                memberRepository.findProjectionsByUsername("m1");
//        List<UsernameOnlyDto> result =
//                memberRepository.findProjectionsDtoByUsername("m1", UsernameOnlyDto.class);
//
//        for (UsernameOnlyDto usernameOnly : result) {
//            System.out.println("usernameOnly = " + usernameOnly.getUsername());
//        }
        List<NestedClosedProjections> result =
                memberRepository.findProjectionsDtoByUsername("m1", NestedClosedProjections.class);
        for (NestedClosedProjections nestedClosedProjections : result) {
            System.out.println("nestedClosedProjections = " + nestedClosedProjections.getUsername());
            System.out.println("nestedClosedProjections = " + nestedClosedProjections.getTeam().getName());

        }
        //then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void nativeQuery() {
//given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();
//when
//        Member result = memberRepository.findByNativeQuery("m1");
        Page<MemberProjections> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjections> content = result.getContent();
        for (MemberProjections memberProjections : content) {
            System.out.println("memberProjections = " + memberProjections.getUsername());
            System.out.println("memberProjections = " + memberProjections.getTeamName());
        }
        System.out.println("result = " + result);
//then
//        Assertions.assertThat(result.size()).isEqualTo(1);
    }
}

