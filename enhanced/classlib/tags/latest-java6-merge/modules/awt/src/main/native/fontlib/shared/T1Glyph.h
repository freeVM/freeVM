/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Viskov Nikolay 
 */
#ifndef __TYPE_1_GLYPH_CLASS_H
#define __TYPE_1_GLYPH_CLASS_H

#include <stack>
#include "Glyph.h"
#include "Outline.h"
#include "Type1Structs.h"

class T1Glyph : public Glyph {
private:
	Type1Map *_charStringMap;
	Type1Map *_subrsMap;

    ffloat _relativeSize;
    ffloat _glyphBB[4]; 

	void parseValueToOutline(EncodedValue *value, std::stack<ffloat> *stack, Outline *out, ffloat *curX, ffloat *curY, ffloat relativeSize);
	void countPoints(std::stack<ffloat> *stack, EncodedValue *value, ufshort *point, ufshort *command);

public:
	T1Glyph(Type1Map *charStringMap, Type1Map *subrsMap, ufshort unicode, ufshort size, ffloat relativeSize, ffloat* fontBB);
	~T1Glyph();
	Outline* getOutline(void);
	ffloat* getGlyphMetrics(void);
};

#endif //__TYPE_1_GLYPH_CLASS_H
