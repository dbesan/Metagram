package org.studyproject.metagram.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.studyproject.metagram.domain.Message;
import org.studyproject.metagram.domain.User;
import org.studyproject.metagram.domain.dto.MessageDto;

import java.util.List;
import java.util.Optional;

public interface MessageRepo extends CrudRepository<Message, Long> {

    @Query("select new org.studyproject.metagram.domain.dto.MessageDto(" +
            "   m, " +
            "   count(ml), " +
            "   sum(case when ml = :user then 1 else 0 end) > 0" +
            ") " +
            "from Message m left join m.likes ml " +
            "group by m")
    Page<MessageDto> findAll(Pageable pageable, @Param("user") User user);

    @Query("select new org.studyproject.metagram.domain.dto.MessageDto(" +
            "   m, " +
            "   count(ml), " +
            "   sum(case when ml = :user then 1 else 0 end) > 0" +
            ") " +
            "from Message m left join m.likes ml " +
            "where m.tag=:tag " +
            "group by m")
    Page<MessageDto> findByTag(@Param("tag") String tag, Pageable pageable, @Param("user") User user);

    @Query("select new org.studyproject.metagram.domain.dto.MessageDto(" +
            "   m, " +
            "   count(ml), " +
            "   sum(case when ml = :user then 1 else 0 end) > 0" +
            ") " +
            "from Message m left join m.likes ml " +
            "where m.author=:author " +
            "group by m")
    Page<MessageDto> findAllByAuthor(@Param("user") User user, Pageable pageable, @Param("author") User author);


    List<Message> findAllByAuthor(User user);

    Optional<Message> findById(Long id);
}
