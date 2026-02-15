package pages

import kotlinx.html.*
import kotlinx.html.stream.*

fun mainPage(): String {
    return createHTML().html {
        head {
            title { +"Логопедический помощник" }
            style {
                unsafe {
                    +"""
                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 20px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            max-width: 800px;
                            margin: 0 auto;
                            background: white;
                            padding: 30px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        .welcome {
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .sound-list {
                            list-style: none;
                            padding: 0;
                        }
                        .sound-list li {
                            margin: 10px 0;
                        }
                        .sound-list a {
                            display: block;
                            padding: 15px;
                            background: #4CAF50;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                            text-align: center;
                            font-size: 18px;
                        }
                        .sound-list a:hover {
                            background: #45a049;
                        }
                    """
                }
            }
        }
        body {
            div("container") {
                div("welcome") {
                    h1 { +"Здравствуйте!" }
                    p { +"Я Ваш помощник по автоматизации звуков!" }
                    p { +"Выберите звук:" }

                    ul("sound-list") {
                        li { a("/sound/P") { +"Звук 'Р'" } }
                        li { a("/sound/Sh") { +"Звук 'Ш'" } }
                        li { a("/sound/L") { +"Звук 'Л'" } }
                        li { a("/sound/X") { +"Звук 'Х'" } }
                    }
                }
            }
        }
    }
}