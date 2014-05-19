
/*
 * This verilog module takes three inputs and a clock. On the 
 * positive edge of the clock, the inputs are and'ed together
 * and the result is placed on the output pin.
 */

module and3(clk, i1, i2, i3, o1);
    input clk;
    input i1;
    input i2;
    input i3;
    output o1;

    reg o1_reg;
    assign o1 = o1_reg;

    always @(posedge clk)
    begin
        o1_reg <= i1 & i2 & i3;
    end

    task dispState;
        begin
            $display("%0t: displayState: i1: 'b%0b, i2: 'b%0b, i3: 'b%0b, i1: 'b%0b", $time, i1, i2, i3, o1);
        end
    endtask

endmodule

