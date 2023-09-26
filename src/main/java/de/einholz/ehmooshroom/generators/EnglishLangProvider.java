/*
 * Copyright 2023 Einholz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.einholz.ehmooshroom.generators;

import java.nio.file.Path;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class EnglishLangProvider extends CustomLangProvider {

    public EnglishLangProvider(FabricDataGenerator generator, String code) {
        super(generator, code);
    }

    public void generateTranslations(TranslationBuilder translationBuilder) {
        // translationBuilder.add(SIMPLE_ITEM, "Simple Item");
        // translationBuilder.add(SIMPLE_BLOCK, "Simple Block");
        // translationBuilder.add(SIMPLE_ITEM_GROUP, "Simple Item Group");
        try {
            Path path = getPath();
            translationBuilder.add(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add existing language file!", e);
        }
    }
}
