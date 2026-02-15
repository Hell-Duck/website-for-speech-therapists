package pages

import data.GameData
import kotlinx.html.*
import kotlinx.html.stream.*
import models.RoomObject

fun FlowContent.renderGameObject(obj: RoomObject, hasSound: Boolean) {
    val escapedName = obj.name.replace("'", "\\'")

    // Контейнер для предмета (не кликабельный)
    div {
        attributes["class"] = "object-container"
        attributes["style"] = "left: ${obj.x}px; top: ${obj.y}px; position: absolute;"

        // Кнопка-предмет (кликабельная)
        button {
            attributes["class"] = if (hasSound) "room-object" else "room-object no-r-sound"
            attributes["onclick"] = "window.game.handleClick(this, '${escapedName}', '${obj.id}', $hasSound)"

            if (obj.imagePath != null) {
                img {
                    src = obj.imagePath
                    alt = obj.name
                    attributes["class"] = "object-image"
                    attributes["style"] = "width: 60px; height: 60px; object-fit: contain;"
                }
            }
            // Иначе показываем эмодзи
            else if (obj.emoji != null) {
                div {
                    attributes["class"] = "object-emoji"
                    +obj.emoji
                }
            }
            div {
                attributes["class"] = "object-name"
                +obj.name
            }
        }

        // Отдельная кнопка для звука (маленькая, в углу)
        button {
            attributes["class"] = "sound-btn"
            attributes["onclick"] = "event.stopPropagation(); window.game.speak('${escapedName}');"
            +"🔊"
        }
    }
}

fun wordFindGame(): String {
    return createHTML().html {
        head {
            title { +"Игра 'Найди слово' - Звук Р" }
            style {
                unsafe {
                    +"""
                        body {
                            font-family: 'Arial', sans-serif;
                            margin: 0;
                            padding: 20px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                        }
                        
                        .object-container {
                            position: absolute;
                        }
                        
                        .room-object {
                            width: 100px;
                            height: 120px;
                            background: white;
                            border: 4px solid #4CAF50;
                            border-radius: 15px;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            cursor: pointer;
                            transition: all 0.3s;
                            padding: 12px;
                            box-shadow: 0 6px 12px rgba(0,0,0,0.15);
                            border: none; /* Убираем стандартные границы кнопки */
                            font-family: inherit;
                        }
                        
                        .room-object:hover {
                            transform: scale(1.1) translateY(-5px);
                            background: linear-gradient(145deg, #e8f5e9, #c8e6c9);
                            border-color: #2E7D32;
                        }
                        
                        .room-object.no-r-sound {
                            border-color: #9E9E9E;
                            background: #EEEEEE;
                        }
                        
                        .sound-btn {
                            position: absolute;
                            bottom: 5px;
                            right: 5px;
                            width: 30px;
                            height: 30px;
                            background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
                            color: white;
                            border: none;
                            border-radius: 50%;
                            cursor: pointer;
                            font-size: 16px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            z-index: 10;
                            box-shadow: 0 2px 5px rgba(0,0,0,0.2);
                        }
                        
                        .sound-btn:hover {
                            transform: scale(1.1);
                            background: linear-gradient(135deg, #1976D2 0%, #0d47a1 100%);
                        }
                        
                        /* Убираем стандартные стили кнопки */
                        button {
                            background: none;
                            border: none;
                            padding: 0;
                            margin: 0;
                        }
                        
                        .game-wrapper { max-width: 1000px; margin: 0 auto; }
                        .game-container {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            padding: 30px;
                            border-radius: 20px;
                            box-shadow: 0 20px 40px rgba(0,0,0,0.3);
                        }
                        .game-header {
                            background: white;
                            padding: 20px 30px;
                            border-radius: 15px;
                            margin-bottom: 20px;
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                        }
                        .game-header h1 {
                            margin: 0;
                            color: #333;
                            font-size: 24px;
                        }
                        .score {
                            background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
                            color: white;
                            padding: 10px 25px;
                            border-radius: 25px;
                            font-weight: bold;
                        }
                        .room {
                            background: #F5DEB3;
                            padding: 30px;
                            border-radius: 20px;
                            position: relative;
                            min-height: 600px;
                            border: 8px solid #8B4513;
                            background-image: radial-gradient(circle at 10px 10px, rgba(139,69,19,0.1) 2px, transparent 2px);
                            background-size: 30px 30px;
                        }
                        
                        .room-object.speaking {
                            animation: speakPulse 0.5s ease;
                        }
                        @keyframes speakPulse {
                            0%, 100% { transform: scale(1); }
                            50% { transform: scale(1.2); background: #FFE082; }
                        }
                        .object-image {
                            width: 60px;
                            height: 60px;
                            object-fit: contain;
                            margin-bottom: 5px;
                        }
                        .object-emoji { font-size: 48px; margin-bottom: 8px; }
                        .object-name { 
                            font-size: 15px; 
                            font-weight: bold; 
                            color: #333;
                            text-align: center;
                        }
                        .message-box {
                            background: white;
                            padding: 20px;
                            border-radius: 15px;
                            margin-top: 20px;
                            text-align: center;
                            font-size: 18px;
                            border-left: 5px solid #4CAF50;
                        }
                        .back-btn {
                            background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%);
                            color: white;
                            padding: 12px 25px;
                            border: none;
                            border-radius: 25px;
                            cursor: pointer;
                            text-decoration: none;
                            display: inline-block;
                            transition: all 0.3s;
                        }
                        .back-btn:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
                        }
                        .found-badge {
                            position: absolute;
                            top: -10px;
                            right: -10px;
                            background: #FF5722;
                            color: white;
                            border-radius: 50%;
                            width: 30px;
                            height: 30px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 18px;
                            font-weight: bold;
                        }
                        .speak-btn {
                            background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);
                            color: white;
                            padding: 6px 12px;
                            border: none;
                            border-radius: 15px;
                            cursor: pointer;
                            font-size: 12px;
                            margin-top: 8px;
                            transition: all 0.3s;
                        }
                        .speak-btn:hover {
                            transform: scale(1.05);
                            background: linear-gradient(135deg, #1976D2 0%, #0d47a1 100%);
                        }
                        .window {
                            position: absolute;
                            top: 30px;
                            right: 30px;
                            width: 120px;
                            height: 100px;
                            background: #87CEEB;
                            border: 4px solid #5D4037;
                            border-radius: 10px;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                        }
                        .door {
                            position: absolute;
                            bottom: 0;
                            left: 30px;
                            width: 70px;
                            height: 130px;
                            background: #8B4513;
                            border: 4px solid #5D4037;
                            border-bottom: none;
                            border-radius: 10px 10px 0 0;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: flex-end;
                            padding-bottom: 10px;
                        }
                        .door-handle {
                            position: absolute;
                            top: 50%;
                            right: 10px;
                            width: 15px;
                            height: 15px;
                            background: #FFD700;
                            border-radius: 50%;
                        }
                        .carpet {
                            position: absolute;
                            bottom: 0;
                            left: 20%;
                            right: 20%;
                            height: 20px;
                            background: #D2691E;
                            border-radius: 20px 20px 0 0;
                            opacity: 0.5;
                        }
                        .back-link {
                            margin-top: 20px;
                            text-align: center;
                        }
                    """
                }
            }
            script {
                unsafe {
                    +"""
                        class WordFindGame {
                            constructor() {
                                this.score = 0;
                                this.foundObjects = new Set();
                                this.totalTargets = ${GameData.soundRObjects.size};
                            }
                            
                            speak(text) {
                                if ('speechSynthesis' in window) {
                                    window.speechSynthesis.cancel();
                                    const utterance = new SpeechSynthesisUtterance(text);
                                    utterance.lang = 'ru-RU';
                                    utterance.rate = 0.9;
                                    utterance.pitch = 1.1;
                                    window.speechSynthesis.speak(utterance);
                                } else {
                                    alert('Ваш браузер не поддерживает речевой синтез');
                                }
                            }
                            
                            animateObject(element) {
                                element.classList.add('speaking');
                                setTimeout(() => element.classList.remove('speaking'), 500);
                            }
                            
                            handleClick(element, name, id, hasSound) {
                                this.animateObject(element);
                                if (hasSound && !this.foundObjects.has(id)) {
                                    this.foundObjects.add(id);
                                    this.score++;
                                    const scoreElement = document.getElementById('score-value');
                                    if (scoreElement) {
                                        scoreElement.textContent = this.score + '/' + this.totalTargets;
                                    }
                                    const badge = document.createElement('div');
                                    badge.className = 'found-badge';
                                    badge.textContent = '✓';
                                    element.style.position = 'relative';
                                    element.appendChild(badge);
                                    this.speak(name);
                                    if (this.score === this.totalTargets) {
                                        setTimeout(() => alert('🎉 Поздравляем! Вы нашли все предметы со звуком "Р"!'), 500);
                                    }
                                } else {
                                    this.speak(name);
                                }
                            }
                        }
                        window.onload = function() { window.game = new WordFindGame(); };
                    """
                }
            }
        }
        body {
            div {
                attributes["class"] = "game-wrapper"
                div {
                    attributes["class"] = "game-container"
                    div {
                        attributes["class"] = "game-header"
                        h1 { +"🎮 Игра 'Найди слово'" }
                        div {
                            attributes["class"] = "score"
                            +"Найдено: "
                            span {
                                id = "score-value"
                                +"0/${GameData.soundRObjects.size}"
                            }
                        }
                    }

                    // КОМНАТА — используем сокращенную форму для статики
                    div("room") {
                        // Окно (статика)
                        div("window") {
                            div {
                                style = "font-size: 24px;"
                                +"☁️☁️"
                            }
                            div {
                                style = "margin-top: 5px; color: #333; font-weight: bold;"
                                +"Окно"
                            }
                        }
                        // Дверь (статика)
                        div("door") {
                            div("door-handle")
                            div {
                                style = "color: white; font-weight: bold;"
                                +"Дверь"
                            }
                        }
                        // Ковёр (статика)
                        div("carpet")

                        // ПРЕДМЕТЫ СО ЗВУКОМ Р — используем функцию
                        GameData.soundRObjects.forEach { obj ->
                            renderGameObject(obj, true)
                        }

                        // ПРЕДМЕТЫ БЕЗ ЗВУКА Р — используем функцию
                        GameData.nonSoundRObjects.forEach { obj ->
                            renderGameObject(obj, false)
                        }
                    }

                    div("message-box") {
                        p {
                            +"✨ Нажимай на предметы, чтобы услышать их название! "
                            +"Найди все предметы со звуком "
                            strong { +"[Р]" }
                        }
                    }

                    div {
                        attributes["class"] = "back-link"
                        a {
                            attributes["class"] = "back-btn"
                            href = "/sound/P"
                            +"← Вернуться к упражнениям"
                        }
                    }
                }
            }
        }
    }
}