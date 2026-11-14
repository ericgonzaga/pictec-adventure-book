package pictec.adventurebook.services.respositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pictec.adventurebook.domain.entities.BookMetadata;

public interface BookMetadataRepository extends JpaRepository<BookMetadata, String> {

  @Query("""
      select b from BookMetadata b
      where (lower(b.title) like lower(concat('%', :q, '%'))
             or lower(b.author) like lower(concat('%', :q, '%')))
        and (:category is null or b.category = :category)
        and (:difficulty is null or b.difficulty = :difficulty)
      order by b.title
      """)
  List<BookMetadata> search(@Param("q") String q,
      @Param("category") String category,
      @Param("difficulty") String difficulty);
}
