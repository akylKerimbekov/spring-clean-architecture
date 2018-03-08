package pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.entity.mutable;

import lombok.Data;

import java.time.Instant;

/**
 * @author <a href="krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszyński</a>
 * @since 2018-03-08
 */
@Data
public class FormerOwnershipData {
  private OwnershipData ownershipData;
  private Instant to;

  public FormerOwnershipData() {
    // do nothing
  }
}
