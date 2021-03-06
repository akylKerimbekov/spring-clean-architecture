package pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.entity;

import lombok.RequiredArgsConstructor;
import pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.configuration.BeanFactoryProvider;
import pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.metadata.HasMetadata;
import pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.metadata.Metadata;
import pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.metadata.MetadataEntry;
import pl.gov.coi.cleanarchitecture.example.spring.pets.domain.model.metadata.Reference;
import pl.wavesoftware.utils.stringify.ObjectStringifier;
import pl.wavesoftware.utils.stringify.configuration.DoNotInspect;
import pl.wavesoftware.utils.stringify.configuration.Mode;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

import static pl.wavesoftware.eid.utils.EidPreconditions.checkState;

/**
 * @author <a href="mailto:krzysztof.suszynski@coi.gov.pl">Krzysztof Suszynski</a>
 * @since 23.04.18
 */
public abstract class AbstractEntity<T extends AbstractEntity> implements HasMetadata<T> {
  @Nullable
  @DoNotInspect
  private Supplier<Metadata<T>> metadataSupplier;

  @Override
  public Metadata<T> getMetadata() {
    return Optional.ofNullable(metadataSupplier)
      .map(Supplier::get)
      .orElse(new NoopMetadata<>(getEntityClass()));
  }

  @Override
  public void supplierOfMetadata(Supplier<Metadata<T>> metadataSupplier) {
    if (this.metadataSupplier != null) {
      @SuppressWarnings("unchecked")
      AbstractEntity<T> helperEntity = (AbstractEntity<T>) new AbstractEntity<AbstractEntity>() {};
      helperEntity.supplierOfMetadata(metadataSupplier);
      checkState(
        hashCode() == helperEntity.hashCode(),
        "20180516:103250",
        "Can't set metadata that returns other hashCode, then previously set metadata"
      );
    }
    this.metadataSupplier = metadataSupplier;
  }

  @Override
  public boolean isMetadataSet() {
    return Optional.ofNullable(metadataSupplier).isPresent();
  }

  @Override
  public String toString() {
    ObjectStringifier stringifier = new ObjectStringifier(this);
    stringifier.setMode(Mode.PROMISCUOUS);
    stringifier.setBeanFactory(BeanFactoryProvider.getBeanFactory());
    return stringifier.toString();
  }

  @SuppressWarnings("unchecked")
  private Class<T> getEntityClass() {
    return (Class<T>) this.getClass();
  }

  @Override
  public int hashCode() {
    return getMetadata()
      .get(Reference.class)
      .map(reference -> reference.get().hashCode())
      .orElse(System.identityHashCode(this));
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    }
    if (!(object instanceof AbstractEntity)) {
      return false;
    }
    if (!(object.getClass().isAssignableFrom(getClass())
      || getClass().isAssignableFrom(object.getClass()))) {
      return false;
    }
    @SuppressWarnings("unchecked")
    final AbstractEntity<T> other = (AbstractEntity<T>) object;
    return equalsByMetadataReference(other);
  }

  private boolean equalsByMetadataReference(AbstractEntity<T> other) {
    Optional<Reference> thisRef = getMetadata().get(Reference.class);
    Optional<Reference> otherRef = other.getMetadata().get(Reference.class);
    return thisRef.isPresent()
      && otherRef.isPresent()
      && thisRef.get().isEqualTo(otherRef.get());
  }

  @RequiredArgsConstructor
  private static final class NoopMetadata<T> implements Metadata<T> {
    private final Class<T> cls;

    @Override
    public <I, D extends MetadataEntry<I>> Optional<D> get(Class<D> metadataClass) {
      return Optional.empty();
    }

    @Override
    public Class<T> type() {
      return cls;
    }
  }
}
