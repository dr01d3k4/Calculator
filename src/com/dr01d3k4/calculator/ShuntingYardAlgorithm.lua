local tinsert = table.insert;
local tremove = table.remove;


-- Off topic note, I like the javadoc style comments, might write a plugin that parses Lua(/Moonscript) code for them


--[[
Returns true if the input is a valid number

@param input
		the input to check

@return boolean
		whether the input is a number
]]
local function isNumber(input)
	return (tonumber(input) and true or false);
end


--[[
Returns true if the input is *, /, + or -

@param input
		the input to check

@return boolean
		whether the input is an operator
]]
local function isOperator(input)
	return (input == "+") or (input == "-") or (input == "*") or (input == "/");
end


--[[
Returns true if the input is ( or )

@param input
		the input to check

@return boolean
		whether the input is a bracket
]]
local function isBracket(input)
	return (input == "(") or (input == ")");
end


--[[
Returns true if the input is an identifier (not a number, bracket or operator)

@param input
		the input to check

@return boolean
		whether the input is an identifier
]]
local function isFunction(input)
	return (not isNumber(input) and not isOperator(input) and not isBracket(input));
end


local precedences = { };
-- This table and loop makes organizing precedence easier and finding at runtime quicker
do
	local precedenceByGroup = {
		{"+", "-"},
		{"*", "/", "%"},
		{"^"}
	};

	for group = 1, #precedenceByGroup, 1 do
		for _, operator in pairs(precedenceByGroup[group]) do
			precedences[operator] = group;
		end
	end
end


--[[
Returns the precedence of an operator

@param operator
		the operator to find the precedence of

@return precedence
]]
local function getPrecedence(operator)
	return precedences[operator];
end;


local leftAssociative = {
	["^"] = true;
	["*"] = true;
	["/"] = true;
	["%"] = true;
	["+"] = true;
	["-"] = true;
};


--[[
Returns whether an operator is left associative

@param operator
		the operator to find the associativity

@return boolean
]]
local function isLeftAssociative(operator)
	return leftAssociative[operator];
end


--[[
Returns an array in string form.
tableToString({1, 5, 8}) returns "{1, 5, 8}"

@param tbl
		the table to cast to string

@return string
]]
local function tableToString(tbl)
	local str = "{";
	for i = 1, #tbl, 1 do
		local value = ((type(tbl[i]) == "function") and tableToString(tbl[i]) or tostring(tbl[i]));
		str = str..value..", ";
	end
	str = str:sub(1, -3).."}";
	return str;
end


--[[
Compiles a mathematical operation in infix notation to RPN
Algorithm from http://en.wikipedia.org/wiki/Shunting-yard_algorithm
Comments in the code are from the algorithm description


@param tokenStream
		an array of tokens in the form {"token1", "token2", "token3"}

@return rpn
		the token stream in RPN
]]
local function shuntingYardAlgorithm(tokenStream)
	local putOnOutputQueue, getOutputQueue;
	-- Declarion and functions for the output in this block
	do
		local output = { };

		putOnOutputQueue = function (value)
			tinsert(output, value);
		end;

		getOutputQueue = function ()
			return output;
		end
	end

	local pushStack, popStack, peekStack;
	-- Declaration and functions for the stack in this block
	do
		local stack = { };

		pushStack = function (value)
			tinsert(stack, tostring(value));
		end;

		popStack = function ()
			if (#stack == 0) then
				return nil;
			else
				return tostring(tremove(stack));
			end
		end;

		peekStack = function ()
			if (#stack == 0) then
				return nil;
			else
				return tostring(stack[#stack]);
			end
		end;
	end

	-- While there are tokens to read
	-- Read a token
	for currentToken = 1, #tokenStream, 1 do
		local token = tostring(tokenStream[currentToken]);

		-- If the token is a number, then add it to the output queue
		if (isNumber(token)) then
			putOnOutputQueue(token);

		-- If the token is a function token, then push it onto the stack
		elseif (isFunction(token)) then
			pushStack(token);

		-- If the token is an operator, o1, then
		elseif (isOperator(token)) then
			local o1 = token;
			local o2 = peekStack();

			if (o2 and isOperator(o2)) then
				local o1Precedence = getPrecedence(o1);
				local o1LeftAssociative = isLeftAssociative(o1);

				local o2Precedence = getPrecedence(o2);

				-- While there is an operator token, o2, at the top of the stack and
				-- 		either o1 is left-associative and its precedence is equal to that of o2
				--		or o1 has precendence less than that of o2
				while ((o1LeftAssociative and (o1Precedence == o2Precedence)) or (o1Precedence < o2Precedence)) do
					-- Pop o2 off the stack, onto the output queue
					putOnOutputQueue(popStack());

					o2 = peekStack();
					if (o2 and isOperator(o2)) then
						o2Precedence = getPrecedence(o2);
					else
						break;
					end
				end
			end

			-- Push o1 onto the stack
			pushStack(o1);

		-- If the token is a left parenthesis, then push it onto the stack
		elseif (token == "(") then
			pushStack(token);

		-- If the token is a right parenthesis
		elseif (token == ")") then
			-- Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue
			-- Pop the left parenthesis from the stack, but not onto the output queue
			-- If the token at the top of the stack is a function token, pop it onto the output queue
			-- If the stack runs out without finding a left parenthesis, then there are mismatched parentheses

			local topOfStack = peekStack();
			while (topOfStack and (topOfStack ~= "(")) do
				putOnOutputQueue(popStack());
				topOfStack = peekStack();
			end

			if (topOfStack == "(") then
				popStack();

				topOfStack = peekStack();
				if (topOfStack and isFunction(topOfStack)) then
					putOnOutputQueue(popStack());
				end

			else
				error("Mismatched parentheses");
			end

		end
	end

	-- When there are no more tokens to read
	-- While there are still operator tokens in the stack
	local topOfStack = peekStack();
	while (topOfStack) do
		-- If the operator token at the top of the stack is a parenthesis, then there are mismatched parentheses
		if (isBracket(topOfStack)) then
			error("Mismatched parentheses");
		end

		-- Pop the operator onto the output queue
		putOnOutputQueue(popStack());
		topOfStack = peekStack();
	end

	-- Exit
	return getOutputQueue();
end


--[[
Takes a string and tokenizes it
tokenize("5 + 2 * 3") returns {5, +, 2, *, 3}

@param str
		the string to tokenize

@return array
		the array of tokens
]]
local function tokenize(str)
	local insertToken, getTokens;
	do
		local tokens = { };

		insertToken = function (token)
			if (token ~= "") then
				tinsert(tokens, token);
			end
		end;

		getTokens = function ()
			return tokens;
		end;
	end


	local getNextChar, peekNextChar;
	do
		local c = 0;

		getNextChar = function ()
			repeat
				c = c + 1;
			until (str:sub(c, c):match("^%S$") or (c > #str));

			if (c <= #str) then
				return str:sub(c, c);
			else
				return nil;
			end
		end;

		peekNextChar = function ()
			local tempC = c;
			repeat
				tempC = tempC + 1;
			until (str:sub(tempC, tempC):match("^%S$") or (tempC > #str));

			if (tempC <= #str) then
				return str:sub(tempC, tempC);
			else
				return nil;
			end
		end;
	end

	local current = "";
	local previousWasOperator = 0;

	local char = getNextChar();
	while (char) do
		previousWasOperator = previousWasOperator - 1;

		if (isNumber(char) or (char == ".")) then
			current = current..char;

		elseif (isOperator(char)) then
			insertToken(current);
			current = "";
			if ((char == "-") and (previousWasOperator > 0)) then
				if (isNumber(peekNextChar())) then
					current = "-";
				else
					insertToken("-");
				end
			else
				insertToken(char);
			end
			previousWasOperator = 2;

		elseif (isBracket(char)) then
			insertToken(current);
			current = "";
			insertToken(char);
		
		else
			current = current..char;
		end

		char = getNextChar();
	end

	insertToken(current);

	return getTokens();
end


--[[
Evaluates the answer from an array of tokens representing a calculation in RPN

@param rpnTokens
		the array of tokens

@return answer
		the numerical answer
]]
local function evaluateRPN(rpnTokens)
	local stack = { };

	local function pushStack(value)
		tinsert(stack, value);
	end

	local function popStack()
		return tremove(stack);
	end

	local functions = {
		["+"] = function ()
			local op2 = popStack();
			local op1 = popStack();
			pushStack(op1 + op2);
		end;

		["-"] = function ()
			local op2 = popStack();
			local op1 = popStack();
			pushStack(op1 - op2);
		end;

		["*"] = function ()
			local op2 = popStack();
			local op1 = popStack();
			pushStack(op1 * op2);
		end;

		["/"] = function ()
			local op2 = popStack();
			local op1 = popStack();
			pushStack(op1 / op2);
		end;

		["sin"] = function ()
			pushStack(math.sin(popStack()));
		end;
	};

	for t = 1, #rpnTokens, 1 do
		local token = rpnTokens[t];
		if (isNumber(token)) then
			pushStack(tonumber(token));
		
		elseif (functions[token]) then
			functions[token]();
		end
	end

	if (#stack ~= 1) then
		error("Error evaluating RPN (size of stack not 1 at end of calculation");
	end
	return stack[1];
end


--[[
Evaluates the value of a sum in infix notation by:
1) Tokenizing
2) Using the shunting-yard algorithm to rewrite as RPN
3) Evaluating the RPN

@param sum
		the sum as a string to evaluate

@return answer
		the numerical answer
]]
local function evaluateSum(sum)
	print("");
	print("Evaluating sum \""..sum.."\"");

	local tokens = tokenize(sum);
	print(tableToString(tokens));

	local rpn = shuntingYardAlgorithm(tokens);
	print(tableToString(rpn));

	local answer = evaluateRPN(rpn);
	print(answer);

	print("");
	return answer;
end

evaluateSum("5 + 2 * 3");
evaluateSum("(5 + 2) * 3");
evaluateSum("5 + 2 * 3 - -8.2 / 10");
evaluateSum("5 * sin(2 * 1 - 3)");