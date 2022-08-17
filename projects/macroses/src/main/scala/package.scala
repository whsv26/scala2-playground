package org.whsv26.playground

package object macroses {
  trait HasId[Id] {
    def id: Id
  }

  trait IdExtractor[T] {
    def extract(t: T): String
  }

  object IdExtractor {
    def apply[T: IdExtractor]: IdExtractor[T] = implicitly[IdExtractor[T]]

    /**
     * Derives type class instance for String and String @newtype identifiers
     * Entity must implement HasId trait
     */
    implicit def instance[T]: IdExtractor[T] = macro IdExtractorDerivation.impl[T]
  }
}
