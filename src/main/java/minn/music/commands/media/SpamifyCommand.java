/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.commands.media;

import minn.music.commands.GenericCommand;

public class SpamifyCommand extends GenericCommand
{

	public enum Chars
	{
		ZERO('０'), ONE('１'), TWO('２'), THREE('３'), FOUR('４'), FIVE('５'), SIX('６'), SEVEN('７'), EIGHT('８'), NINE('９'),
		A('Ａ'), a('ａ'),
		B('Ｂ'), b('ｂ'),
		C('Ｃ'), c('ｃ'),
		D('Ｄ'), d('ｄ'),
		E('Ｅ'), e('ｅ'),
		F('Ｆ'), f('ｆ'),
		G('Ｇ'), g('ｇ'),
		H('Ｈ'), h('ｈ'),
		I('Ｉ'), i('ｉ'),
		J('Ｊ'), j('ｊ'),
		K('Ｋ'), k('ｋ'),
		L('Ｌ'), l('ｌ'),
		M('Ｍ'), m('ｍ'),
		N('Ｎ'), n('ｎ'),
		O('Ｏ'), o('ｏ'),
		P('Ｐ'), p('ｐ'),
		Q('Ｑ'), q('ｑ'),
		R('Ｒ'), r('ｒ'),
		S('Ｓ'), s('ｓ'),
		T('Ｔ'), t('ｔ'),
		U('Ｕ'), u('ｕ'),
		V('Ｖ'), v('ｖ'),
		W('Ｗ'), w('ｗ'),
		X('Ｘ'), x('ｘ'),
		Y('Ｙ'), y('ｙ'),
		Z('Ｚ'), z('ｚ'),
		EXCLAMATION('！'), APOSTROPHE('＇'), QUESTION('？'), DOUBLE_POINTS('：');

		public char getValue()
		{
			return value;
		}

		private final char value;

		Chars(char v)
		{
			this.value = v;
		}

		public String toString()
		{
			return String.valueOf(getValue());
		}
	}

	public static String convert(String string)
	{
		String converted = "";

		string = string
				.replace("0", "" + Chars.ZERO)
				.replace("1", "" + Chars.ONE)
				.replace("2", "" + Chars.TWO)
				.replace("3", "" + Chars.THREE)
				.replace("4", "" + Chars.FOUR)
				.replace("5", "" + Chars.FIVE)
				.replace("6", "" + Chars.SIX)
				.replace("7", "" + Chars.SEVEN)
				.replace("8", "" + Chars.EIGHT)
				.replace("9", "" + Chars.NINE)
				.replace(":", "" + Chars.DOUBLE_POINTS)
				.replace("!", "" + Chars.EXCLAMATION)
				.replace("?", "" + Chars.QUESTION)
				.replace("'", "" + Chars.APOSTROPHE);
		boolean found;
		for (char c : string.toCharArray())
		{
			found = false;
			for (Chars match : Chars.values())
			{
				if (!match.name().equals("" + c))
					continue;
				converted += match;
				found = true;
				break;
			}
			if(!found)
				converted += c;
		}

		return converted;
	}

	@Override
	public String getAlias()
	{
		return "spam";
	}

	public String getAttributes()
	{
		return "<text>";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if(event.allArgs.isEmpty())
			event.send("Enter something.");
		else
			event.send(convert(event.allArgs));
	}

}
