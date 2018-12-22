package pureconfig

/**
 * A version of `ConfigCursor` with a more fluent interface, focused on config navigation instead of error handling.
 *
 * The `at` method, used to access object and list values, is available without a previous need to cast the cursor and
 * always returns another cursor instead of a `ReaderResult`. The error handling is left for the last step, where users
 * can opt to cast to a primitive value using one of the `as` methods or by requesting a regular cursor with `cursor`.
 *
 * @param cursor the regular cursor pointed to by this object, wrapped itself into a `Right`, or a `Left` with a list of
 *               failures in case an error occurred during navigation
 */
case class FluentConfigCursor(cursor: ReaderResult[ConfigCursor]) {

  /**
   * Casts this cursor to a string.
   *
   * @return a `Right` with the string value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asString: ReaderResult[String] = cursor.right.flatMap(_.asString)

  /**
   * Casts this cursor to a boolean.
   *
   * @return a `Right` with the boolean value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asBoolean: ReaderResult[Boolean] = cursor.right.flatMap(_.asBoolean)

  /**
   * Casts this cursor to a long.
   *
   * @return a `Right` with the long value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asLong: ReaderResult[Long] = cursor.right.flatMap(_.asLong)

  /**
   * Casts this cursor to an int.
   *
   * @return a `Right` with the int value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asInt: ReaderResult[Int] = cursor.right.flatMap(_.asInt)

  /**
   * Casts this cursor to a short.
   *
   * @return a `Right` with the short value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asShort: ReaderResult[Short] = cursor.right.flatMap(_.asShort)

  /**
   * Casts this cursor to a double.
   *
   * @return a `Right` with the double value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asDouble: ReaderResult[Double] = cursor.right.flatMap(_.asDouble)

  /**
   * Casts this cursor to a float.
   *
   * @return a `Right` with the float value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asFloat: ReaderResult[Float] = cursor.right.flatMap(_.asFloat)

  /**
   * Casts this cursor to a `ConfigListCursor`.
   *
   * @return a `Right` with this cursor as a list cursor if the cast can be done, `Left` with a list of failures
   *         otherwise.
   */
  def asListCursor: ReaderResult[ConfigListCursor] = cursor.right.flatMap(_.asListCursor)

  /**
   * Casts this cursor to a `ConfigObjectCursor`.
   *
   * @return a `Right` with this cursor as an object cursor if it points to an object, `Left` with a list of failures
   *         otherwise.
   */
  def asObjectCursor: ReaderResult[ConfigObjectCursor] = cursor.right.flatMap(_.asObjectCursor)

  /**
   * Returns a cursor to the config at a given path.
   *
   * @param segments the path of the config for which a cursor should be returned
   * @return a `FluentConfigCursor` pointing to the provided path.
   */
  def at(segments: PathSegment*): FluentConfigCursor = FluentConfigCursor {
    segments.foldLeft(this.cursor) {
      case (Right(cur), PathSegment.Key(k)) => cur.asObjectCursor.right.flatMap(_.atKey(k))
      case (Right(cur), PathSegment.Index(i)) => cur.asListCursor.right.flatMap(_.atIndex(i))
      case (Left(err), _) => Left(err)
    }
  }

  /**
   * Casts this cursor to a `ConfigListCursor` and maps each element to a result. This method tries to map all
   * elements, combining failures from all of them if more than one exists.
   *
   * @param f the function used to map elements
   * @tparam A the result type of the elements
   * @return a `Right` with the list obtained by mapping all elements of the list pointed to by this cursor if all
   *         casts and mappings can be done, `Left` with a list of failures otherwise.
   */
  def mapList[A](f: ConfigCursor => ReaderResult[A]) =
    asListCursor.right.flatMap { listCur => ReaderResult.sequence(listCur.list.map(f)) }

  /**
   * Casts this cursor to a `ConfigObjectCursor` and maps each value to a result. This method tries to map all
   * elements, combining failures from all of them if more than one exists.
   *
   * @param f the function used to map values
   * @tparam A the result type of the values
   * @return a `Right` with the map obtained by mapping all values of the object pointed to by this cursor if all
   *         casts and mappings can be done, `Left` with a list of failures otherwise.
   */
  def mapObject[A](f: ConfigCursor => ReaderResult[A]): ReaderResult[Map[String, A]] =
    asObjectCursor.right.flatMap { objCur =>
      val kvResults = objCur.map.map { case (key, cur) => f(cur).right.map((key, _)) }
      ReaderResult.sequence[(String, A), Iterable](kvResults).right.map(_.toMap)
    }
}
