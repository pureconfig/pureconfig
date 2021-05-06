package pureconfig.generic.derivation

extension [A](a: A)
  inline def expandType[B]: A & B =
    inline a match
      case b: B => b
