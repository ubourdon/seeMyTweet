$(document).ready ->
  TWEET.retrieveQuoters()
  TWEET.hideTable()

@TWEET =

  retrieveQuoters: ->
    $("#quoters").submit ->
      userQuoted = $("#tweetLogin").val().toLowerCase()

      if userQuoted != ""
        $.ajax(
          {
            type: "GET"
            url: "/quoters?name=#{userQuoted}"
            dataType: "json"
            success: (data) ->
              if data.length > 0
                TWEET.displayResults data
              else
                TWEET.showError(data, userQuoted)
            error: (data) ->
              TWEET.showError(data, userQuoted)
              console.log "error : " + data
          }
        )

      false

  hideTable: ->
    $("#quotedResults").hide()

  cleanBefore: ->
    $("#errorLogin").hide()
    $("#quotedResults table tbody").empty()

  displayResults: (data) ->
    TWEET.cleanBefore()
    $("#resultNumber").text("#{data.length} résultats trouvés")
    $("#quotedResults").show()
    $.each(data, (k, value) ->
      $($("#quotedResults table tbody")).append("<tr><td><img src='#{value.user.image_url}' alt='user_image' /></td><td>#{value.user.name}</td><td>#{value.text}</td><td>#{TWEET.displayHashtags value.hashtags}</td></tr>")
    )

  showError: (data, userQuoted) ->
    TWEET.cleanBefore()
    TWEET.hideTable()
    $("#errorLogin").html("aucune mention trouvée pour l'utilisateur <span class='userQuoted'>#{userQuoted}</span>").show()

  displayHashtags: (hashtags) ->
    if hashtags.length > 0
      (hashtags.map (elem) ->
        "#" + elem.text
      ).join("<br/>")
    else
      ""