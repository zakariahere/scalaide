package example

final case class User(name: String, age: Int):
  def greeting: String = s"Hello, $name!"

object Main:
  def main(args: Array[String]): Unit =
    val user = User("Zak", 30)
    println(user.greeting)