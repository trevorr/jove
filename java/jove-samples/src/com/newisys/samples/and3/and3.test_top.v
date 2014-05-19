module and3_test_top;
    parameter simulation_cycle = 100;

    reg SystemClock;
    wire clk;
    wire i1;
    wire i2;
    wire i3;
    wire o1;

    assign clk = SystemClock;

    and3_shell vshell();

    and3 dut(
        .clk(clk),
        .i1(i1),
        .i2(i2),
        .i3(i3),
        .o1(o1));


    initial
    begin
        SystemClock = 0;
        forever
        begin
            #(simulation_cycle / 2)
            SystemClock = ~SystemClock;
        end
    end

endmodule

