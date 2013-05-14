package controllers

import play.api._
import libs.json.JsValue
import libs.ws.WS
import play.api.mvc._
import concurrent.{Await, Future}
import concurrent.duration.Duration
import concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

    def index = Action {
        Ok(Await.result(retrieveMentionTweets, Duration.Inf))
    }

    def retrieveMentionTweets: Future[JsValue] = {
        WS.url("http://search.twitter.com/search.json?q=%40ugobourdon&src=typd").withQueryString(
            "page" -> "1",
            "include_entities" -> "true",
            "rpp" -> "100"
        ).get()
        .map { response =>
            response.status match {
                case 200 => response.json
                case x => response.json
            }
        }
    }

    // tweets mentionnant @ugobourdon : http://search.twitter.com/search.json?q=%40ugobourdon&src=typd

    // tweets 5 derniers favoris @ugobourdon : https://api.twitter.com/1/favorites.json?count=5&screen_name=ugobourdon
}