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

package minn.music.hooks;

import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

@FunctionalInterface public interface MentionListener
{
	/**
	 * Get's called when the Bot was mentioned. <b>Don't forget to register this listener.</b>
	 * <br/>
	 * <i>Example can be found in {@link minn.music.hooks.impl.PrefixTeller PreifxTeller}.</i>
	 * @param event A GuildMessageReceivedEvent (We don't care about private mentions here)
	 */
	void onMention(GuildMessageReceivedEvent event);

}
