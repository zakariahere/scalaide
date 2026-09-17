package example

class UserSuite extends munit.FunSuite:
  test("greeting uses the user's name") {
    assertEquals(User("Zak", 30).greeting, "Hello, Zak!")
  }

  test("case class copy preserves the original user") {
    val original = User("Zak", 30)
    val updated = original.copy(age = 31)
    assertEquals(original.age, 30)
    assertEquals(updated.age, 31)
  }
