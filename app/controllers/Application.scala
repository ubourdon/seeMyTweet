package controllers

import play.api._
import libs.ws.WS
import play.api.mvc._
import concurrent.{Await, Future}
import concurrent.duration.Duration
import concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

object Application extends Controller {

    def index = Action { Ok(views.html.index()) }

    def retrieveQuoters(userQuoted: String) = Action {
        Ok(Await.result(toJson(retrieveMentionTweets), Duration.Inf))
    }

    private def toJson(result: Future[Seq[Tweet]]): Future[JsValue] = result.map( tweet => Json.toJson(tweet) )

    private def retrieveMentionTweets: Future[Seq[Tweet]] = {
        WS.url("http://search.twitter.com/search.json?q=%40ugobourdon&src=typd").withQueryString(
            "page" -> "1",
            "include_entities" -> "true",
            "rpp" -> "100"
        ).get()
        .map { response =>
            response.status match {
                case 200 => response.json.asOpt[Seq[Tweet]].getOrElse(Nil)
                case x => Nil
            }
        }
    }

    implicit val readTweet: Reads[Seq[Tweet]] =
        (__ \ "results").read(
            seq(
                (__ \ "from_user_name").read[String] and
                (__ \ "from_user").read[String] and
                (__ \ "text").read[String] and
                (__ \ "entities" \ "hashtags").readNullable(
                    seq(
                        (__ \ "text").read[String]
                    )
                ) and
                (__ \ "profile_image_url").read[String]

                tupled
            )
        ).map( _.collect {
            case (userLogin, userName, text, Some(hashtags), image) => Tweet(User(userLogin, userName, image), text, hashtags.toList)
            case (userLogin, userName, text, None, image) => Tweet(User(userLogin, userName, image), text, Nil)
        })

    implicit val writeUserAsJson: Writes[User] = (
        (__ \ "login").write[String] and
        (__ \ "name").write[String] and
        (__ \ "image_url").write[String]
    )(unlift(User.unapply))

    implicit val writeTweetAsJson: Writes[Tweet] = (
        (__ \ "user").write(writeUserAsJson) and
        (__ \ "text").write[String] and
        (__ \ "hashtags").write( Writes.seq( (__ \ "text").write[String] ) )
        )(unlift(Tweet.unapply))
}

case class Tweet(user: User, content: String, hashtags: List[String])
case class User(login: String, name: String, icon_url: String)

// tweets mentionnant @ugobourdon : http://search.twitter.com/search.json?q=%40ugobourdon&src=typd
// tweets 5 derniers favoris @ugobourdon : https://api.twitter.com/1/favorites.json?count=5&screen_name=ugobourdon