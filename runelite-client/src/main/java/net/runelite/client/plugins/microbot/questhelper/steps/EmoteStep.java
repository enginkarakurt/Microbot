/*
 * Copyright (c) 2020, Zoinkwiz <https://github.com/Zoinkwiz>
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.steps;

import net.runelite.client.plugins.microbot.questhelper.QuestHelperPlugin;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.QuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.steps.emote.QuestEmote;
import net.runelite.client.plugins.microbot.questhelper.steps.overlay.IconOverlay;
import net.runelite.api.ScriptID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EmoteStep extends DetailedQuestStep
{
	private boolean hasScrolled;
	private final QuestEmote emote;

	public EmoteStep(QuestHelper questHelper, QuestEmote emote, WorldPoint worldPoint, String text, Requirement... requirements)
	{
		super(questHelper, worldPoint, text, requirements);
		this.emote = emote;
	}

	public EmoteStep(QuestHelper questHelper, QuestEmote emote, String text, Requirement... requirements)
	{
		super(questHelper, text, requirements);
		this.emote = emote;
	}

	@Override
	public void makeWidgetOverlayHint(Graphics2D graphics, QuestHelperPlugin plugin)
	{
		super.makeWidgetOverlayHint(graphics, plugin);

		Widget emoteContainer = client.getWidget(InterfaceID.Emote.CONTENTS);

		if (emoteContainer == null || emoteContainer.isHidden())
		{
			return;
		}

		Widget emoteWindow = client.getWidget(InterfaceID.Emote.UNIVERSE);

		if (emoteWindow == null)
		{
			return;
		}

		Widget finalEmoteWidget = null;

		for (Widget emoteWidget : emoteContainer.getDynamicChildren())
		{
			if (emoteWidget.getSpriteId() == emote.getSpriteId())
			{
				finalEmoteWidget = emoteWidget;
				graphics.setColor(new Color(questHelper.getConfig().targetOverlayColor().getRed(),
					questHelper.getConfig().targetOverlayColor().getGreen(),
					questHelper.getConfig().targetOverlayColor().getBlue(), 65));
				graphics.fill(emoteWidget.getBounds());
				graphics.setColor(questHelper.getConfig().targetOverlayColor());
				graphics.draw(emoteWidget.getBounds());
			}
		}
		if (!hasScrolled)
		{
			hasScrolled = true;
			scrollToWidget(finalEmoteWidget);
		}
	}

	void scrollToWidget(Widget widget)
	{
		final Widget parent = client.getWidget(InterfaceID.Emote.CONTENTS);

		if (widget == null || parent == null)
		{
			return;
		}

		final int newScroll = Math.max(0, Math.min(parent.getScrollHeight(),
			(widget.getRelativeY() + widget.getHeight() / 2) - parent.getHeight() / 2));

		client.runScript(
			ScriptID.UPDATE_SCROLLBAR,
				InterfaceID.Emote.SCROLLBAR,
				InterfaceID.Emote.CONTENTS,
			newScroll
		);
	}

	@Override
	protected void setupIcon()
	{
		if (emote.getSpriteId() != -1 && icon == null)
		{
			BufferedImage emoteImage = spriteManager.getSprite(emote.getSpriteId(), 0);
			if (emoteImage != null)
			{
				icon = IconOverlay.createIconImage(emoteImage);
			}
		}
		super.setupIcon();
	}
}
