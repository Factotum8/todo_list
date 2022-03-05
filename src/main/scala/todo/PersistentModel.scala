package todo

import cats.implicits.*
import java.nio.file.{Path, Paths, Files}
import java.nio.charset.StandardCharsets
import io.circe.{Decoder, Encoder}
import io.circe.parser.*
import io.circe.syntax.*
import scala.collection.mutable
import todo.data.*

/**
 * The PersistentModel is a model that saves all data to files, meaning that
 * tasks persist between restarts.
 */
object PersistentModel extends Model:
  import Codecs.given

  /** Path where the tasks are saved */
  val tasksPath = Paths.get("tasks.json")
  /** Path where the next id is saved */
  val idPath = Paths.get("id.json")
  val defaultTasks = List(
    Id(0) -> Task(State.completedNow, "Complete Effective Scala Week 2", None, List(Tag("programming"), Tag("scala"))),
    Id(1) -> Task(State.Active, "Complete Effective Scala Week 3", Some("Finish the todo list exercise"), List(Tag("programming"), Tag("scala"), Tag("encapsulation"), Tag("sbt"))),
    Id(2) -> Task(State.Active, "Make a sandwich", Some("Cheese and salad or ham and tomato?"), List(Tag("food"), Tag("lunch")))
  )
  private var idStore = loadId()
  private val idGenerator = IdGenerator(idStore)
  private var tasksStore = loadTasks()
  /**
   * Load Tasks from a file. Return an empty task list if the file does not exist,
   * and throws an exception if decoding the file fails.
   */
  def loadTasks(): Tasks =
    if Files.exists(tasksPath) then
      load[Tasks](tasksPath)
    else
      Tasks.empty

  /**
   * Load an Id from a file. Returns Id(0) if the file does not exist, and throws
   * an exception if decoding the file fails.
   */
  def loadId(): Id =
    if Files.exists(idPath) then
      load[Id](idPath)
    else
      Id(0)

  /**
   * Load JSON-encoded data from a file.
   *
   * Given a file name, load JSON data from that file, and decode it into the
   * type A. Throws an exception on failure.
   *
   * It is not necessary to use this method. You should be able to use loadTasks
   * and loadId instead, which have a simpler interface.
   */
  def load[A](path: Path)(using decoder: Decoder[A]): A = {
    val str = Files.readString(path, StandardCharsets.UTF_8)

    decode[A](str) match {
      case Right(result) => result
      case Left(error) => throw error
    }
  }

  /**
   * Save tasks to a file. If the file already exists it is overwritten.
   */
  def saveTasks(tasks: Tasks): Unit =
    save(tasksPath, tasks)

  /**
   * Save Id to a file. If the file already exists it is overwritten.
   */
  def saveId(id: Id): Unit =
    save(idPath, id)

  /**
   * Save data to a file in JSON format.
   *
   * Given a file name and some data, saves that data to the file in JSON
   * format. If the file already exists it is overwritten.
   *
   * It is not necessary to use this method. You should be able to use saveTasks
   * and saveId instead, which have a simpler interface.
   */
  def save[A](path: Path, data: A)(using encoder: Encoder[A]): Unit =
    val json = data.asJson
    Files.writeString(path, json.spaces2, StandardCharsets.UTF_8)
    ()

  def create(task: Task): Id =
    val id = idGenerator.nextId()
    tasksStore = Tasks(tasksStore.toList :+ (id, task))
    saveTasks(tasksStore)
    saveId(id)
    id

  def read(id: Id): Option[Task] =
    tasksStore.toList.find((i, t) => {i == id}) match {
      case Some(i: Id, t: Task) => Some(t)
      case None => None
    }

  def update(id: Id)(f: Task => Task): Option[Task] =
    tasksStore = Tasks(tasksStore.toMap.updatedWith(id)(opt => opt.map(f)))
    tasksStore.toMap.get(id)

  def delete(id: Id): Boolean =
    val before = tasksStore.toList.size
    tasksStore = Tasks(tasksStore.toMap.removed(id))
    // TODO printf("before: " + before.toString + " after: " + tasksStore.toList.size.toString + "\n")
    before > tasksStore.toList.size

  def tasks: Tasks =
    tasksStore

  def tasks(tag: Tag): Tasks =
    Tasks(tasksStore.toMap.filter((K, V) => V.tags.contains(tag)))

  def complete(id: Id): Option[Task] =
    tasksStore.toMap.get(id) match {
      case Some(t: Task) => Option(t.complete)
      case None => None
    }

  def tags: Tags =
    Tags(tasksStore.toList.flatMap(_._2.tags).distinct)

  def clear(): Unit =
    tasksStore = Tasks(List.empty)
